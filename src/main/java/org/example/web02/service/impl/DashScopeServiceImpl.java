package org.example.web02.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.web02.entity.KnowledgeBase;
import org.example.web02.entity.UserHealth;
import org.example.web02.entity.WeightRecord;
import org.example.web02.mapper.UserHealthMapper;
import org.example.web02.mapper.WeightRecordMapper;
import org.example.web02.service.DashScopeService;
import org.example.web02.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * 阿里百炼 DashScope API 服务实现类
 * 负责调用阿里云大模型进行文本生成和健康计划生成
 * 支持用户传入API Key或使用环境变量/D配置文件中的默认密钥
 */
@Service
@Slf4j
public class DashScopeServiceImpl implements DashScopeService {

    /** 阿里百炼文本生成API地址 */
    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    /** 用户健康档案Mapper */
    private final UserHealthMapper userHealthMapper;
    /** 用户体重记录Mapper */
    private final WeightRecordMapper weightRecordMapper;
    /** RAG知识库服务（用于检索用户相关健康知识） */
    private final KnowledgeBaseService knowledgeBaseService;
    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;
    /** HTTP客户端（复用） */
    private final HttpClient httpClient;

    /** 配置文件中的默认API Key */
    @Value("${dashscope.api-key:}")
    private String defaultApiKey;

    /**
     * 构造函数注入依赖
     * @param userHealthMapper 用户健康档案Mapper
     * @param weightRecordMapper 用户体重记录Mapper
     * @param knowledgeBaseService RAG知识库服务
     * @param objectMapper JSON序列化工具
     */
    public DashScopeServiceImpl(UserHealthMapper userHealthMapper,
                                WeightRecordMapper weightRecordMapper,
                                KnowledgeBaseService knowledgeBaseService,
                                ObjectMapper objectMapper) {
        this.userHealthMapper = userHealthMapper;
        this.weightRecordMapper = weightRecordMapper;
        this.knowledgeBaseService = knowledgeBaseService;
        this.objectMapper = objectMapper;
        // 构建HTTP客户端，设置30秒连接超时
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String generateText(String prompt) {
        return generateText(prompt, 3000, null);
    }

    @Override
    public String generateText(String prompt, int maxTokens) {
        return generateText(prompt, maxTokens, null);
    }

    /**
     * 调用阿里百炼大模型生成文本
     * @param prompt 提示词
     * @param maxTokens 最大生成token数
     * @param apiKey 用户传入的API Key（可选）
     * @return 模型生成的文本
     */
    @Override
    public String generateText(String prompt, int maxTokens, String apiKey) {
        // Step 1: 解析API Key（优先级：用户传入 > 配置文件 > 环境变量）
        String effectiveApiKey = resolveApiKey(apiKey);

        if (effectiveApiKey == null || effectiveApiKey.isEmpty()) {
            throw new RuntimeException("未配置阿里百炼API密钥，请设置环境变量DASHSCOPE_API_KEY或在前端输入API Key");
        }

        try {
            // Step 2: 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-turbo");  // 使用通义千问turbo模型

            Map<String, Object> input = new HashMap<>();
            input.put("prompt", prompt);
            requestBody.put("input", input);

            // 参数配置：temperature越低越稳定，top_p控制采样范围
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("result_format", "text");   // 返回纯文本格式
            parameters.put("max_tokens", maxTokens);   // 最大生成长度
            parameters.put("temperature", 0.3);        // 低温度，输出更稳定
            parameters.put("top_p", 0.8);              // 核采样参数
            requestBody.put("parameters", parameters);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.info("AI请求体: {}", jsonBody);

            // Step 3: 构建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + effectiveApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(120))  // 2分钟超时
                    .build();

            // Step 4: 发送请求并获取响应
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("AI响应状态码: {}, 响应体: {}", response.statusCode(), response.body());

            // Step 5: 解析响应
            if (response.statusCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(response.body());
                JsonNode output = responseJson.path("output");
                JsonNode choices = output.path("choices");
                
                // 优先解析choices格式（新版API）
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).path("message");
                    String content = message.path("content").asText();
                    if (content != null && !content.isEmpty()) {
                        return content;
                    }
                }
                
                // 兼容旧版text字段格式
                String text = output.path("text").asText();
                if (text != null && !text.isEmpty()) {
                    return text;
                }
                
                // 兜底返回原始响应
                return response.body();
            } else {
                log.error("阿里百炼API调用失败: status={}, body={}", response.statusCode(), response.body());
                throw new RuntimeException("AI服务调用失败: " + response.statusCode());
            }
        } catch (Exception e) {
            log.error("调用阿里百炼API异常", e);
            throw new RuntimeException("AI服务调用异常: " + e.getMessage());
        }
    }

    /**
     * 解析API Key，按优先级获取
     * 优先级：请求传入 > 配置文件 > 环境变量
     * @param requestApiKey 请求传入的API Key
     * @return 有效的API Key
     */
    private String resolveApiKey(String requestApiKey) {
        // 优先级1：用户请求传入的API Key
        if (requestApiKey != null && !requestApiKey.isEmpty()) {
            return requestApiKey;
        }
        // 优先级2：配置文件中的默认API Key
        if (defaultApiKey != null && !defaultApiKey.isEmpty()) {
            return defaultApiKey;
        }
        // 优先级3：环境变量DASHSCOPE_API_KEY
        return System.getenv("DASHSCOPE_API_KEY");
    }

    @Override
    public Map<String, Object> generateHealthPlan(Long userId) {
        return generateHealthPlan(userId, null);
    }

    /**
     * 生成个性化健康计划（核心方法）
     * 整合用户健康档案、历史体重数据和RAG知识库，调用大模型生成一周饮食运动计划
     * @param userId 用户ID
     * @param apiKey 用户传入的API Key（可选）
     * @return 包含计划标题、总热量、计划内容等的Map
     */
    @Override
    public Map<String, Object> generateHealthPlan(Long userId, String apiKey) {
        // Step 1: 查询用户健康档案
        UserHealth health = userHealthMapper.findByUserId(userId);
        if (health == null) {
            throw new RuntimeException("用户健康档案不存在，请先完善健康信息");
        }

        // Step 2: 查询用户近30天体重记录（用于分析趋势）
        List<WeightRecord> recentWeight = weightRecordMapper.findRecent30DaysWeight(userId);

        // Step 3: 构建用户上下文信息（健康档案）
        StringBuilder context = new StringBuilder();
        context.append("用户健康档案信息：\n");
        context.append("1. 年龄：").append(health.getAge()).append("岁\n");
        context.append("2. 性别：").append(health.getGender() == 0 ? "男" : "女").append("\n");
        context.append("3. 身高：").append(health.getHeight()).append("cm\n");
        context.append("4. 当前体重：").append(health.getWeight()).append("kg\n");
        context.append("5. 活动水平：").append(getActivityLevelDesc(health.getActivityLevel())).append("\n");
        context.append("6. 饮食偏好：").append(health.getDietHobby() != null ? health.getDietHobby() : "未设置").append("\n");
        context.append("7. 健康目标：").append(health.getHealthTarget() != null ? health.getHealthTarget() : "未设置").append("\n");
        context.append("8. 过敏信息：").append(health.getAllergy() != null ? health.getAllergy() : "无").append("\n");
        context.append("9. 病史：").append(health.getMedicalHistory() != null ? health.getMedicalHistory() : "无").append("\n");
        context.append("10. 基础代谢率(BMR)：").append(health.getBmr() != null ? health.getBmr().intValue() : "未计算").append("kcal\n");
        context.append("11. 每日总能量消耗(TDEE)：").append(health.getTdee() != null ? health.getTdee().intValue() : "未计算").append("kcal\n");
        context.append("12. BMI指数：").append(health.getBmi() != null ? health.getBmi() : "未计算").append("\n");

        // 添加近30天体重记录
        if (recentWeight != null && !recentWeight.isEmpty()) {
            context.append("\n近30天体重记录（共").append(recentWeight.size()).append("条）：\n");
            for (WeightRecord record : recentWeight) {
                context.append("- ").append(record.getRecordDate()).append(": ").append(record.getWeight()).append("kg\n");
            }
            context.append("\n");
        }

        // Step 4: RAG检索 - 根据用户标签从知识库获取相关健康知识
        log.warn("=== RAG DEBUG: 准备检索知识库, userId={} ===", userId);
        List<KnowledgeBase> ragKnowledge = knowledgeBaseService.retrieveRelevantKnowledge(userId);
        log.warn("=== RAG DEBUG: 检索完成, 结果数量={} ===", ragKnowledge != null ? ragKnowledge.size() : 0);
        
        // 将检索到的知识追加到上下文
        if (ragKnowledge != null && !ragKnowledge.isEmpty()) {
            context.append("\n【权威健康知识库参考】（请结合以下专业知识制定计划）：\n");
            for (int i = 0; i < ragKnowledge.size(); i++) {
                KnowledgeBase kb = ragKnowledge.get(i);
                context.append((i + 1)).append(". ").append(kb.getTitle()).append("\n");
                context.append(kb.getContent()).append("\n\n");
            }
        }

        // Step 5: 构建完整的Prompt（角色定义+上下文+格式要求）
        String prompt = "你现在是一名专业营养师+持证健身教练。\n" +
                "根据用户提供的健康档案、代谢数据、历史体重、打卡记录以及权威健康知识库内容，为用户生成科学、安全、个性化的一周饮食与运动计划。\n" +
                "要求内容严谨、条理清晰，贴合用户饮食偏好、过敏禁忌与健康目标。\n" +
                "严格按照下方 JSON 格式输出，禁止输出任何额外解释或闲聊。\n\n" +
                context + "\n\n请基于以上用户健康档案信息，生成一份个性化的一周健康计划，请以JSON格式返回，包含以下字段：\n" +
                "{\n" +
                "  \"planTitle\": \"个性化一周健康计划\",\n" +
                "  \"totalCalorie\": 每日建议总热量（数字），\n" +
                "  \"summary\": {\n" +
                "    \"diet\": [\"饮食建议1\", \"饮食建议2\", \"饮食建议3\"],\n" +
                "    \"exercise\": [\"运动建议1\", \"运动建议2\", \"运动建议3\"],\n" +
                "    \"tips\": [\"健康提示1\", \"健康提示2\", \"健康提示3\"]\n" +
                "  },\n" +
                "  \"weeklyPlan\": [\n" +
                "    {\n" +
                "      \"dayName\": \"周一\",\n" +
                "      \"date\": \"1/1\",\n" +
                "      \"diet\": [\n" +
                "        {\"type\": \"早餐\", \"name\": \"食物名称\", \"calorie\": \"热量\"},\n" +
                "        {\"type\": \"午餐\", \"name\": \"食物名称\", \"calorie\": \"热量\"},\n" +
                "        {\"type\": \"晚餐\", \"name\": \"食物名称\", \"calorie\": \"热量\"}\n" +
                "      ],\n" +
                "      \"exercise\": [\n" +
                "        {\"name\": \"运动名称\", \"duration\": \"时长\"},\n" +
                "        {\"name\": \"运动名称\", \"duration\": \"时长\"},\n" +
                "        {\"name\": \"运动名称\", \"duration\": \"时长\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "注意：\n" +
                "1. 每天的饮食和运动安排要有变化，不要重复\n" +
                "2. 根据用户的健康目标（减肥/增肌/维持健康）调整热量摄入\n" +
                "3. 考虑用户的饮食偏好和过敏信息\n" +
                "4. 运动安排要符合用户的活动水平\n" +
                "5. 每天必须提供3项运动安排，每项运动不重复，同时要求这些运动安排要符合实际且具体、细致\n" +
                "6. 每天的饮食安排要符合实际，食材来源易获取，符合中国国内人民的饮食习惯，三餐的安排要符合中国膳食比例，讲究早上吃好、中午吃饱、晚上吃少";

        // Step 6: 调用大模型并处理响应（含降级策略）
        Map<String, Object> result = new HashMap<>();
        result.put("healthInfo", context.toString());

        try {
            // 调用大模型生成计划
            String response = generateText(prompt, 3000, apiKey);
            log.info("AI响应原始内容: {}", response);
            result.put("rawResponse", response);

            // 从响应中提取JSON
            String jsonStr = extractJsonFromResponse(response);
            log.info("提取的JSON字符串: {}", jsonStr);
            
            if (jsonStr != null && !jsonStr.isEmpty()) {
                try {
                    Map<String, Object> planData = objectMapper.readValue(jsonStr, Map.class);
                    
                    // 验证JSON结构完整性
                    if (planData.containsKey("planTitle") || planData.containsKey("weeklyPlan")) {
                        result.put("planTitle", planData.getOrDefault("planTitle", "个性化健康计划"));
                        // 安全转换总热量，默认使用TDEE或2000kcal
                        result.put("totalCalorie", safeToInteger(planData.get("totalCalorie"),
                                health.getTdee() != null ? health.getTdee().intValue() : 2000));

                        // 构建计划内容（仅保留summary和weeklyPlan）
                        Map<String, Object> planContentMap = new HashMap<>();
                        planContentMap.put("summary", planData.get("summary"));
                        planContentMap.put("weeklyPlan", planData.get("weeklyPlan"));
                        result.put("planContent", objectMapper.writeValueAsString(planContentMap));
                        log.info("成功使用AI生成的计划");
                    } else {
                        log.warn("AI响应JSON格式不完整，缺少必要字段，使用默认计划");
                        throw new RuntimeException("AI响应JSON格式不完整");
                    }
                } catch (Exception e) {
                    log.warn("解析AI响应JSON失败: {}", e.getMessage());
                    throw e;
                }
            } else {
                log.warn("无法从AI响应中提取JSON，使用默认计划");
                throw new RuntimeException("无法从AI响应中提取JSON");
            }
        } catch (Exception e) {
            // 降级策略：AI服务不可用时使用本地生成的默认计划
            log.warn("AI服务调用或解析失败，使用默认计划: {}", e.getMessage());
            result.put("planTitle", "个性化健康计划");
            result.put("totalCalorie", health.getTdee() != null ? health.getTdee().intValue() : 2000);
            result.put("rawResponse", "AI服务不可用: " + e.getMessage());

            // 生成本地默认计划
            Map<String, Object> fallbackPlan = generateFallbackPlan(health);
            try {
                result.put("planContent", objectMapper.writeValueAsString(fallbackPlan));
            } catch (Exception ex) {
                log.error("生成默认计划失败", ex);
            }
        }

        return result;
    }

    private String getActivityLevelDesc(int level) {
        return switch (level) {
            case 1 -> "低（久坐少动）";
            case 2 -> "中（轻度运动）";
            case 3 -> "高（经常锻炼）";
            default -> "未知";
        };
    }

    private Integer safeToInteger(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String extractJsonFromResponse(String response) {
        if (response == null) return null;
        
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        return null;
    }

    private Map<String, Object> generateFallbackPlan(UserHealth health) {
        Map<String, Object> plan = new HashMap<>();
        
        Map<String, List<String>> summary = new HashMap<>();
        summary.put("diet", Arrays.asList(
            "保持规律的三餐时间",
            "控制主食摄入量",
            "多吃蔬菜水果"
        ));
        summary.put("exercise", Arrays.asList(
            "每周进行3-5次有氧运动",
            "每次运动30分钟以上",
            "结合力量训练"
        ));
        summary.put("tips", Arrays.asList(
            "保证充足睡眠",
            "多喝水，每天1500-2000ml",
            "保持良好心态"
        ));
        plan.put("summary", summary);
        
        List<Map<String, Object>> weeklyPlan = new ArrayList<>();
        String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        
        for (int i = 0; i < 7; i++) {
            Map<String, Object> dayPlan = new HashMap<>();
            dayPlan.put("dayName", dayNames[i]);
            dayPlan.put("date", (i + 1) + "/1");
            
            List<Map<String, String>> diet = new ArrayList<>();
            if (i % 3 == 0) {
                diet.add(createMeal("早餐", "燕麦粥+水煮蛋+牛奶", "350kcal"));
                diet.add(createMeal("午餐", "糙米饭+鸡胸肉+西兰花", "450kcal"));
                diet.add(createMeal("晚餐", "清蒸鱼+青菜", "300kcal"));
            } else if (i % 3 == 1) {
                diet.add(createMeal("早餐", "全麦面包+豆浆", "320kcal"));
                diet.add(createMeal("午餐", "杂粮饭+牛肉+胡萝卜", "480kcal"));
                diet.add(createMeal("晚餐", "虾仁豆腐+冬瓜", "280kcal"));
            } else {
                diet.add(createMeal("早餐", "玉米+鸡蛋+酸奶", "340kcal"));
                diet.add(createMeal("午餐", "红薯+鱼肉+生菜", "430kcal"));
                diet.add(createMeal("晚餐", "菌菇汤+鸡胸肉", "310kcal"));
            }
            diet.add(createMeal("加餐", "苹果+坚果", "150kcal"));
            dayPlan.put("diet", diet);
            
            List<Map<String, String>> exercise = new ArrayList<>();
            if (i % 2 == 0) {
                exercise.add(createExercise("慢跑", "30分钟"));
                exercise.add(createExercise("拉伸", "10分钟"));
            } else if (i % 4 == 1) {
                exercise.add(createExercise("力量训练", "45分钟"));
                exercise.add(createExercise("瑜伽", "20分钟"));
            } else if (i % 4 == 3) {
                exercise.add(createExercise("游泳", "40分钟"));
                exercise.add(createExercise("放松", "15分钟"));
            } else {
                exercise.add(createExercise("快走", "35分钟"));
            }
            dayPlan.put("exercise", exercise);
            
            weeklyPlan.add(dayPlan);
        }
        plan.put("weeklyPlan", weeklyPlan);
        
        return plan;
    }

    private Map<String, String> createMeal(String type, String name, String calorie) {
        Map<String, String> meal = new HashMap<>();
        meal.put("type", type);
        meal.put("name", name);
        meal.put("calorie", calorie);
        return meal;
    }

    private Map<String, String> createExercise(String name, String duration) {
        Map<String, String> exercise = new HashMap<>();
        exercise.put("name", name);
        exercise.put("duration", duration);
        return exercise;
    }
}