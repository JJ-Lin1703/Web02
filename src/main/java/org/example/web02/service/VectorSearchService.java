package org.example.web02.service;

import org.example.web02.entity.DocumentChunk;
import org.example.web02.mapper.DocumentChunkMapper;
import org.example.web02.util.CosineSimilarityUtil;
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
 * 技术栈：阿里百炼text-embedding-v3模型 + 余弦相似度计算
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    /** 文档块Mapper（存储向量数据） */
    private final DocumentChunkMapper documentChunkMapper;
    /** Embedding向量化工具（调用阿里百炼API） */
    private final EmbeddingUtil embeddingUtil;

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
        return doIndex(text, fileName);
    }

    /**
     * 核心入库逻辑：分块 → 向量化 → 入库
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

        // Step 2: 逐块向量化 + 组装实体
        List<DocumentChunk> chunkList = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);

            // 调用阿里百炼embedding模型向量化
            List<Float> vector = embeddingUtil.embed(chunkText);

            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocId(docId);
            chunk.setDocName(fileName);
            chunk.setChunkIndex(i);
            chunk.setChunkText(chunkText);
            chunk.setEmbedding(embeddingUtil.serialize(vector));  // 向量序列化为JSON字符串
            chunkList.add(chunk);

            log.info("块 {}/{} 已向量化", i + 1, chunks.size());
        }

        // Step 3: 批量入库
        documentChunkMapper.insertBatch(chunkList);
        log.info("文档入库完成：docId={}, 块数={}", docId, chunks.size());

        return docId;
    }

    /**
     * 检索最相关的 topK 个文本块（核心检索方法）
     * 流程：问题向量化 → 加载所有文档块 → 计算余弦相似度 → 排序过滤
     *
     * @param question 用户提问
     * @param topK     返回数量
     * @return 相关文本块列表（按相似度降序）
     */
    public List<DocumentChunk> search(String question, int topK) {
        // Step 1: 将问题向量化（调用阿里百炼embedding模型）
        List<Float> questionVector = embeddingUtil.embed(question);

        // Step 2: 加载所有文档块到内存（当前实现：全量检索）
        List<DocumentChunk> allChunks = documentChunkMapper.findAll();
        if (allChunks.isEmpty()) {
            log.warn("知识库为空，无可检索内容");
            return Collections.emptyList();
        }

        // Step 3: 计算余弦相似度，排序取 topK
        double minSimilarity = 0.5; // 相似度阈值（低于此值的结果被过滤）
        
        // 为每个文档块计算相似度并排序
        List<ChunkWithSimilarity> scored = allChunks.stream()
                .map(chunk -> {
                    // 反序列化存储的向量
                    List<Float> chunkVector = embeddingUtil.deserialize(chunk.getEmbedding());
                    // 计算余弦相似度（越接近1越相似）
                    double similarity = CosineSimilarityUtil.cosine(questionVector, chunkVector);
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

        // 记录未找到相关内容的情况
        if (result.isEmpty()) {
            log.info("未找到相似度≥{}的相关内容，最高相似度={}", minSimilarity,
                    scored.isEmpty() ? 0 : String.format("%.4f", scored.get(0).similarity));
        }
        return result;
    }

    /**
     * 获取所有已上传文档列表
     */
    public List<String> listDocs() {
        return documentChunkMapper.findDistinctDocs();
    }

    /**
     * 删除指定文档
     */
    @Transactional
    public void deleteDoc(String docId) {
        documentChunkMapper.deleteByDocId(docId);
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