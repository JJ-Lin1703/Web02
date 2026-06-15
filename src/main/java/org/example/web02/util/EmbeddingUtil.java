package org.example.web02.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Embedding 向量化工具类
 * 调用阿里百炼 DashScope text-embedding-v3 模型
 *
 * 限流策略：
 *   4 并发线程 + 4 QPS 令牌桶限流，避免触发 API 限流
 *   HTTP 连接池复用，减少 TCP 握手开销
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingUtil {

    /** 阿里百炼 Embedding API 地址（OpenAI 兼容模式） */
    private static final String EMBEDDING_API_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings";

    /** Embedding 模型名称 */
    private static final String MODEL = "text-embedding-v4";

    /** 单次批量请求最大条数（阿里百炼限制：不超过10） */
    private static final int BATCH_SIZE = 10;

    /** 并发线程数 */
    private static final int CONCURRENT_THREADS = 4;

    /** 最大 QPS（与并发线程数一致） */
    private static final int MAX_QPS = 4;

    /** 限流：相邻请求最小间隔（毫秒） */
    private static final long MIN_INTERVAL_MS = 1000 / MAX_QPS;

    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;

    /** API Key（优先从环境变量读取） */
    private volatile String apiKey;

    /** 复用 HttpClient（自带连接池，默认 keep-alive 5 分钟） */
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .executor(Executors.newFixedThreadPool(CONCURRENT_THREADS))
            .build();

    /** 限流锁对象 */
    private final Object rateLimitLock = new Object();
    /** 上次请求时间戳（毫秒） */
    private long lastRequestTime = 0;

    /**
     * 获取 API Key
     */
    private String getApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("DASHSCOPE_API_KEY");
        }
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("未配置阿里百炼API密钥，请设置环境变量DASHSCOPE_API_KEY");
        }
        return apiKey;
    }

    /**
     * 将文本转为向量（单条）
     */
    public List<Float> embed(String text) {
        List<List<Float>> results = batchEmbed(Collections.singletonList(text));
        return results.isEmpty() ? Collections.emptyList() : results.get(0);
    }

    /**
     * QPS 限流：确保每秒不超过 MAX_QPS 次请求
     */
    private void acquireRateLimitPermit() {
        synchronized (rateLimitLock) {
            long now = System.currentTimeMillis();
            long waitMs = MIN_INTERVAL_MS - (now - lastRequestTime);
            if (waitMs > 0) {
                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lastRequestTime = System.currentTimeMillis();
        }
    }

    /**
     * 批量将文本转为向量（并发批量调用 + QPS 限流）
     */
    public List<List<Float>> batchEmbed(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<String>> batches = partition(texts, BATCH_SIZE);
        log.info("开始批量向量化：{} 个文本，分成 {} 批，每批最多 {} 条，{} 线程，{} QPS",
                texts.size(), batches.size(), BATCH_SIZE, CONCURRENT_THREADS, MAX_QPS);

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        List<CompletableFuture<List<List<Float>>>> futures = new ArrayList<>();

        for (int i = 0; i < batches.size(); i++) {
            final int batchIndex = i;
            final List<String> batch = batches.get(i);
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return doBatchEmbed(batch, batchIndex);
                } catch (Exception e) {
                    log.error("批次 {} 向量化失败: {}", batchIndex, e.getMessage());
                    throw new RuntimeException("批次 " + batchIndex + " 向量化失败: " + e.getMessage(), e);
                }
            }, executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // 汇总结果并保持原有顺序
        List<List<Float>> allResults = new ArrayList<>(texts.size());
        int textIndex = 0;
        for (int i = 0; i < batches.size(); i++) {
            List<List<Float>> batchResult = futures.get(i).join();
            for (List<Float> vector : batchResult) {
                if (textIndex < texts.size()) {
                    allResults.add(vector);
                    textIndex++;
                }
            }
        }

        log.info("批量向量化完成：共 {} 个向量", allResults.size());
        return allResults;
    }

    /**
     * 执行单次批量请求（含限流 + 连接池复用）
     */
    private List<List<Float>> doBatchEmbed(List<String> texts, int batchIndex) {
        // 1) 获取 QPS 限流许可
        acquireRateLimitPermit();

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("input", texts);
            // OpenAI 兼容模式不需要 parameters 参数

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.info("批次 {} 请求中（{} 条，{} 字符）: {}",
                    batchIndex, texts.size(),
                    texts.stream().mapToInt(String::length).sum(),
                    jsonBody.length() > 500 ? jsonBody.substring(0, 500) + "..." : jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EMBEDDING_API_URL))
                    .header("Authorization", "Bearer " + getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            // 复用 httpClient 实例（自带连接池）
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("批次 {} 响应状态: {}, 响应长度: {}",
                    batchIndex, response.statusCode(), response.body().length());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    List<List<Float>> results = new ArrayList<>();
                    for (JsonNode item : data) {
                        JsonNode embedding = item.path("embedding");
                        List<Float> vector = new ArrayList<>();
                        for (JsonNode val : embedding) {
                            vector.add(val.floatValue());
                        }
                        results.add(vector);
                    }
                    log.debug("批次 {} 完成：{} 个向量", batchIndex, results.size());
                    return results;
                }
                log.error("批次 {} API 响应格式错误，data 不是数组: {}", batchIndex, response.body());
                throw new RuntimeException("API 响应格式错误");
            } else {
                log.error("批次 {} API 响应异常：{} {}", batchIndex, response.statusCode(), 
                        response.body().length() > 1000 ? response.body().substring(0, 1000) : response.body());
                throw new RuntimeException("Embedding 调用失败：" + response.statusCode());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("批次 {} Embedding 异常", batchIndex, e);
            throw new RuntimeException("Embedding 调用失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将列表按指定大小分区
     */
    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    /**
     * 将向量序列化为 JSON 字符串
     */
    public String serialize(List<Float> vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (Exception e) {
            throw new RuntimeException("向量序列化失败", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为向量
     */
    public List<Float> deserialize(String json) {
        try {
            JsonNode arr = objectMapper.readTree(json);
            List<Float> result = new ArrayList<>();
            for (JsonNode val : arr) {
                result.add(val.floatValue());
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("向量反序列化失败", e);
        }
    }
}