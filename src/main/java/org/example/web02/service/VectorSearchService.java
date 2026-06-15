package org.example.web02.service;

import org.example.web02.entity.DocumentChunk;
import org.example.web02.mapper.DocumentChunkMapper;
import org.example.web02.util.CosineSimilarityUtil;
import org.example.web02.util.EmbeddingCache;
import org.example.web02.util.EmbeddingUtil;
import org.example.web02.util.TextChunkUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量存储与检索服务（文档RAG核心组件）
 * 负责将上传文档分块、向量化、存储，并提供相似度检索功能
 * 技术栈：阿里百炼text-embedding-v3模型 + 余弦相似度计算 + Caffeine缓存优化
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    /** 文档块Mapper（存储向量数据） */
    private final DocumentChunkMapper documentChunkMapper;
    /** Embedding向量化工具（调用阿里百炼API） */
    private final EmbeddingUtil embeddingUtil;
    /** Embedding缓存工具（优化检索性能） */
    private final EmbeddingCache embeddingCache;

    /**
     * 将纯文本索引入库（入口方法）
     * @param text 文本内容
     * @param fileName 文件名
     * @return 文档ID
     */
    @Transactional
    public String indexText(String text, String fileName) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("文本内容为空，无法处理");
        }
        String docId = doIndex(text, fileName);
        // 文档更新后清空缓存，确保下次检索使用最新数据
        embeddingCache.invalidateDocumentChunks();
        return docId;
    }

    /**
     * 核心入库逻辑：分块 → 批量向量化 → 入库
     * @param text 文本内容
     * @param fileName 文件名
     * @return 文档ID
     */
    private String doIndex(String text, String fileName) {
        // 生成唯一文档ID
        String docId = UUID.randomUUID().toString().replace("-", "");

        // Step 1: 文本分块（500字符/块，50字符重叠）
        List<String> chunks = TextChunkUtil.split(text);
        if (chunks.isEmpty()) {
            throw new RuntimeException("文本分块后无内容");
        }
        log.info("文本分块完成：{} 个块", chunks.size());

        // Step 2: 批量向量化（并发批量调用，大幅提升速度）
        List<List<Float>> vectors = embeddingUtil.batchEmbed(chunks);
        if (vectors.isEmpty()) {
            throw new RuntimeException("向量化失败：未返回任何向量");
        }
        if (vectors.size() != chunks.size()) {
            throw new RuntimeException("向量化失败：返回向量数量(" + vectors.size() + ")与输入块数量(" + chunks.size() + ")不一致");
        }

        // Step 3: 组装实体
        List<DocumentChunk> chunkList = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocId(docId);
            chunk.setDocName(fileName);
            chunk.setChunkIndex(i);
            chunk.setChunkText(chunks.get(i));
            chunk.setEmbedding(embeddingUtil.serialize(vectors.get(i)));  // 向量序列化为JSON字符串
            chunkList.add(chunk);
        }

        // Step 4: 批量入库
        documentChunkMapper.insertBatch(chunkList);
        log.info("文档入库完成：docId={}, 块数={}", docId, chunks.size());

        return docId;
    }

    /**
     * 检索最相关的 topK 个文本块（核心检索方法）
     * 流程：问题向量化 → 加载所有文档块 → 计算余弦相似度 → 排序过滤
     * 优化：三层缓存（问题向量缓存 + 文档块缓存 + 检索结果缓存）
     *
     * @param question 用户提问
     * @param topK     返回数量
     * @return 相关文本块列表（按相似度降序）
     */
    public List<DocumentChunk> search(String question, int topK) {
        // Step 0: 检查检索结果缓存（最高优先级，直接返回）
        List<DocumentChunk> cachedResult = embeddingCache.getSearchResult(question);
        if (cachedResult != null) {
            log.debug("检索结果缓存命中：question={}", question.substring(0, Math.min(30, question.length())));
            return cachedResult;
        }

        // Step 1: 将问题向量化（优先使用缓存，避免重复调用API）
        List<Float> questionVector = embeddingCache.getQuestionVector(question);
        if (questionVector == null) {
            questionVector = embeddingUtil.embed(question);
            embeddingCache.putQuestionVector(question, questionVector);
            log.debug("问题向量缓存已更新：question={}", question.substring(0, Math.min(30, question.length())));
        } else {
            log.debug("问题向量缓存命中：question={}", question.substring(0, Math.min(30, question.length())));
        }

        // Step 2: 加载所有文档块到内存（优先使用缓存，减少DB查询）
        List<DocumentChunk> allChunks = embeddingCache.getDocumentChunks();
        if (allChunks == null) {
            allChunks = documentChunkMapper.findAll();
            if (!allChunks.isEmpty()) {
                embeddingCache.putDocumentChunks(allChunks);
                log.debug("文档块缓存已更新：{} 个块", allChunks.size());
            }
        } else {
            log.debug("文档块缓存命中：{} 个块", allChunks.size());
        }

        if (allChunks.isEmpty()) {
            log.warn("知识库为空，无可检索内容");
            return Collections.emptyList();
        }

        // Step 3: 计算余弦相似度，排序取 topK
        double minSimilarity = 0.5; // 相似度阈值（低于此值的结果被过滤）
        
        // 创建 final 副本用于 lambda 表达式
        final List<Float> finalQuestionVector = questionVector;
        
        // 为每个文档块计算相似度并排序
        List<ChunkWithSimilarity> scored = allChunks.stream()
                .map(chunk -> {
                    // 反序列化存储的向量
                    List<Float> chunkVector = embeddingUtil.deserialize(chunk.getEmbedding());
                    // 计算余弦相似度（越接近1越相似）
                    double similarity = CosineSimilarityUtil.cosine(finalQuestionVector, chunkVector);
                    return new ChunkWithSimilarity(chunk, similarity);
                })
                .sorted((a, b) -> Double.compare(b.similarity, a.similarity))  // 降序排序
                .collect(Collectors.toList());

        // Step 4: 过滤低相似度结果，取topK
        List<DocumentChunk> result = scored.stream()
                .filter(cs -> cs.similarity >= minSimilarity)  // 过滤低相似度
                .limit(topK)                                   // 取前topK
                .peek(cs -> log.debug("chunk#{}, similarity={}, text={}...",
                        cs.chunk.getChunkIndex(),
                        String.format("%.4f", cs.similarity),
                        cs.chunk.getChunkText().substring(0, Math.min(50, cs.chunk.getChunkText().length()))))
                .map(cs -> cs.chunk)
                .collect(Collectors.toList());

        // Step 5: 缓存检索结果
        embeddingCache.putSearchResult(question, result);

        // 记录未找到相关内容的情况
        if (result.isEmpty()) {
            log.info("未找到相似度≥{}的相关内容，最高相似度={}", minSimilarity,
                    scored.isEmpty() ? 0 : String.format("%.4f", scored.get(0).similarity));
        }
        return result;
    }

    /**
     * 获取所有已上传文档列表
     * 
     * @return 文档名称列表（去重）
     */
    public List<String> listDocs() {
        return documentChunkMapper.findDistinctDocs();
    }

    /**
     * 删除指定文档
     * 
     * @param docId 文档ID
     */
    @Transactional
    public void deleteDoc(String docId) {
        documentChunkMapper.deleteByDocId(docId);
        // 删除文档后清空缓存，确保下次检索使用最新数据
        embeddingCache.invalidateDocumentChunks();
        log.info("文档已删除：docId={}", docId);
    }

    /**
     * 内部类：块与相似度绑定
     */
    private static class ChunkWithSimilarity {
        final DocumentChunk chunk;
        final double similarity;

        ChunkWithSimilarity(DocumentChunk chunk, double similarity) {
            this.chunk = chunk;
            this.similarity = similarity;
        }
    }
}