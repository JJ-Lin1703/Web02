package org.example.web02.util;

import org.example.web02.entity.DocumentChunk;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Embedding 缓存工具类
 * 优化检索性能，减少重复向量化和数据库查询
 */
@Slf4j
@Component
public class EmbeddingCache {

    /** 问题向量缓存：问题文本 → 向量 */
    private final Cache<String, List<Float>> questionVectorCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /** 文档块缓存：全量文档块列表 */
    private volatile List<DocumentChunk> documentChunksCache;
    private volatile long lastCacheTime = 0;
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5分钟

    /** 检索结果缓存：问题哈希 → 检索结果 */
    private final Cache<String, List<DocumentChunk>> searchResultCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 获取缓存的问题向量
     */
    public List<Float> getQuestionVector(String question) {
        return questionVectorCache.getIfPresent(question);
    }

    /**
     * 缓存问题向量
     */
    public void putQuestionVector(String question, List<Float> vector) {
        questionVectorCache.put(question, vector);
    }

    /**
     * 获取缓存的文档块列表
     */
    public List<DocumentChunk> getDocumentChunks() {
        if (documentChunksCache != null && System.currentTimeMillis() - lastCacheTime < CACHE_DURATION_MS) {
            return documentChunksCache;
        }
        return null;
    }

    /**
     * 缓存文档块列表
     */
    public void putDocumentChunks(List<DocumentChunk> chunks) {
        this.documentChunksCache = chunks;
        this.lastCacheTime = System.currentTimeMillis();
        log.info("文档块缓存已更新：{} 个块", chunks.size());
    }

    /**
     * 获取缓存的检索结果
     */
    public List<DocumentChunk> getSearchResult(String question) {
        return searchResultCache.getIfPresent(question);
    }

    /**
     * 缓存检索结果
     */
    public void putSearchResult(String question, List<DocumentChunk> result) {
        searchResultCache.put(question, result);
    }

    /**
     * 清空所有缓存（文档更新时调用）
     */
    public void invalidateAll() {
        questionVectorCache.invalidateAll();
        documentChunksCache = null;
        searchResultCache.invalidateAll();
        log.info("EmbeddingCache 已清空");
    }

    /**
     * 清空文档块缓存（仅文档更新时）
     */
    public void invalidateDocumentChunks() {
        documentChunksCache = null;
        searchResultCache.invalidateAll(); // 检索结果也需要失效
        log.info("文档块缓存已清空");
    }

    /**
     * 获取缓存统计信息
     */
    public String getStats() {
        return String.format("问题向量缓存: 命中率=%.2f%%, 条目数=%d | 检索结果缓存: 命中率=%.2f%%, 条目数=%d",
                questionVectorCache.stats().hitRate() * 100,
                questionVectorCache.estimatedSize(),
                searchResultCache.stats().hitRate() * 100,
                searchResultCache.estimatedSize());
    }
}