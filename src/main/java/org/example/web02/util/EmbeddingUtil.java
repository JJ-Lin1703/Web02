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

/**
 * Embedding 向量化工具类
 * 调用阿里百炼 DashScope text-embedding-v3 模型
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingUtil {

    private static final String EMBEDDING_API_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings";

    private static final String MODEL = "text-embedding-v3";

    private final ObjectMapper objectMapper;

    private volatile String apiKey;

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
     * 将文本转为向量
     *
     * @param text 输入文本
     * @return 向量（浮点数列表）
     */
    public List<Float> embed(String text) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("input", text);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("text_type", "document");
            requestBody.put("parameters", parameters);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.debug("Embedding请求： {} 字符", text.length());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EMBEDDING_API_URL))
                    .header("Authorization", "Bearer " + getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");
                if (data.isArray() && data.size() > 0) {
                    JsonNode embedding = data.get(0).path("embedding");
                    List<Float> result = new ArrayList<>();
                    for (JsonNode val : embedding) {
                        result.add(val.floatValue());
                    }
                    log.info("Embedding 完成：{} 维向量", result.size());
                    return result;
                }
            }

            log.error("Embedding API 响应异常：{} {}", response.statusCode(), response.body());
            throw new RuntimeException("Embedding 调用失败：" + response.statusCode());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Embedding 异常", e);
            throw new RuntimeException("Embedding 调用失败：" + e.getMessage(), e);
        }
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