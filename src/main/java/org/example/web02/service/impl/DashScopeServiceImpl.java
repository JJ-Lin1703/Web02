package org.example.web02.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.web02.entity.UserHealth;
import org.example.web02.entity.WeightRecord;
import org.example.web02.mapper.UserHealthMapper;
import org.example.web02.mapper.WeightRecordMapper;
import org.example.web02.service.DashScopeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class DashScopeServiceImpl implements DashScopeService {

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    private final UserHealthMapper userHealthMapper;
    private final WeightRecordMapper weightRecordMapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${dashscope.api-key:}")
    private String apiKey;

    public DashScopeServiceImpl(UserHealthMapper userHealthMapper,
                                WeightRecordMapper weightRecordMapper,
                                ObjectMapper objectMapper) {
        this.userHealthMapper = userHealthMapper;
        this.weightRecordMapper = weightRecordMapper;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("DASHSCOPE_API_KEY");
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("未配置阿里百炼API密钥，请设置环境变量DASHSCOPE_API_KEY");
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-turbo");

            Map<String, Object> input = new HashMap<>();
            input.put("prompt", prompt);
            requestBody.put("input", input);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("result_format", "text");
            parameters.put("max_tokens", 2000);
            parameters.put("temperature", 0.3);
            parameters.put("top_p", 0.8);
            requestBody.put("parameters", parameters);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.info("AI请求体: {}", jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(120))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("AI响应状态码: {}, 响应体: {}", response.statusCode(), response.body());

            if (response.statusCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(response.body());
                JsonNode output = responseJson.path("output");
                JsonNode choices = output.path("choices");
                
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).path("message");
                    String content = message.path("content").asText();
                    if (content != null && !content.isEmpty()) {
                        return content;
                    }
                }
                
                String text = output.path("text").asText();
                if (text != null && !text.isEmpty()) {
                    return text;
                }
                
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

    @Override
    public Map<String, Object> generateHealthPlan(Long userId) {
        UserHealth health = userHealthMapper.findByUserId(userId);
        if (health == null) {
            throw new RuntimeException("用户健康档案不存在，请先完善健康信息");
        }

        List<WeightRecord> recentWeight = weightRecordMapper.findRecent30DaysWeight(userId);

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

        if (recentWeight != null && !recentWeight.isEmpty()) {
            context.append("\n近30天体重记录（共").append(recentWeight.size()).append("条）：\n");
            for (WeightRecord record : recentWeight) {
                context.append("- ").append(record.getRecordDate()).append(": ").append(record.getWeight()).append("kg\n");
            }
            context.append("\n");
        }

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
                "        {\"name\": \"运动名称\", \"duration\": \"时长\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "注意：\n" +
                "1. 每天的饮食和运动安排要有变化，不要重复\n" +
                "2. 根据用户的健康目标（减肥/增肌/维持健康）调整热量摄入\n" +
                "3. 考虑用户的饮食偏好和过敏信息\n" +
                "4. 运动安排要符合用户的活动水平";

        Map<String, Object> result = new HashMap<>();
        result.put("healthInfo", context.toString());

        try {
            String response = generateText(prompt);
            log.info("AI响应原始内容: {}", response);
            result.put("rawResponse", response);

            String jsonStr = extractJsonFromResponse(response);
            log.info("提取的JSON字符串: {}", jsonStr);
            
            if (jsonStr != null && !jsonStr.isEmpty()) {
                try {
                    Map<String, Object> planData = objectMapper.readValue(jsonStr, Map.class);
                    
                    if (planData.containsKey("planTitle") || planData.containsKey("weeklyPlan")) {
                        result.put("planTitle", planData.getOrDefault("planTitle", "个性化健康计划"));
                        result.put("totalCalorie", safeToInteger(planData.get("totalCalorie"),
                                health.getTdee() != null ? health.getTdee().intValue() : 2000));

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
            log.warn("AI服务调用或解析失败，使用默认计划: {}", e.getMessage());
            result.put("planTitle", "个性化健康计划");
            result.put("totalCalorie", health.getTdee() != null ? health.getTdee().intValue() : 2000);
            result.put("rawResponse", "AI服务不可用: " + e.getMessage());

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
