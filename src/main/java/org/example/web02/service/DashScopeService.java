package org.example.web02.service;

import java.util.Map;

/**
 * 阿里云DashScope大模型服务接口
 * 提供文本生成和健康计划生成功能
 */
public interface DashScopeService {

    /**
     * 生成文本（使用默认配置）
     * 
     * @param prompt 提示词
     * @return 生成的文本结果
     */
    String generateText(String prompt);

    /**
     * 生成文本（指定最大token数）
     * 
     * @param prompt 提示词
     * @param maxTokens 最大生成token数
     * @return 生成的文本结果
     */
    String generateText(String prompt, int maxTokens);

    /**
     * 生成文本（指定最大token数和API Key）
     * 
     * @param prompt 提示词
     * @param maxTokens 最大生成token数
     * @param apiKey 阿里云DashScope API Key
     * @return 生成的文本结果
     */
    String generateText(String prompt, int maxTokens, String apiKey);

    /**
     * 生成健康计划（使用默认API Key）
     * 
     * @param userId 用户ID
     * @return 健康计划Map，包含饮食、运动等建议
     */
    Map<String, Object> generateHealthPlan(Long userId);

    /**
     * 生成健康计划（使用指定API Key）
     * 
     * @param userId 用户ID
     * @param apiKey 用户传入的API Key（可选）
     * @return 健康计划Map，包含饮食、运动等建议
     */
    Map<String, Object> generateHealthPlan(Long userId, String apiKey);
}
