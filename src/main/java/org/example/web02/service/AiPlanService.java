package org.example.web02.service;

import org.example.web02.dto.request.TweakPlanRequest;
import org.example.web02.dto.response.PageResult;
import org.example.web02.entity.AiPlan;

import java.util.List;

/**
 * AI健康计划服务接口
 * 提供AI健康计划的生成、调整、查询和管理功能
 */
public interface AiPlanService {

    /**
     * 生成AI健康计划（使用默认API Key）
     * 
     * @param userId 用户ID
     * @return AI计划实体
     */
    AiPlan generatePlan(Long userId);

    /**
     * 生成AI健康计划（使用指定API Key）
     * 
     * @param userId 用户ID
     * @param apiKey 用户传入的API Key（可选）
     * @return AI计划实体
     */
    AiPlan generatePlan(Long userId, String apiKey);

    /**
     * 调整AI健康计划（使用默认API Key）
     * 
     * @param userId 用户ID
     * @param request 调整请求，包含用户反馈和偏好
     * @return 调整后的AI计划实体
     */
    AiPlan tweakPlan(Long userId, TweakPlanRequest request);

    /**
     * 调整AI健康计划（使用指定API Key）
     * 
     * @param userId 用户ID
     * @param request 调整请求，包含用户反馈和偏好
     * @param apiKey 用户传入的API Key（可选）
     * @return 调整后的AI计划实体
     */
    AiPlan tweakPlan(Long userId, TweakPlanRequest request, String apiKey);

    /**
     * 获取用户的计划历史列表
     * 
     * @param userId 用户ID
     * @return 计划列表（按版本号倒序）
     */
    List<AiPlan> getPlanHistory(Long userId);

    /**
     * 分页获取用户的计划历史
     * 
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<AiPlan> getPlanHistoryPaginated(Long userId, int pageNum, int pageSize);

    /**
     * 获取用户最新的AI健康计划
     * 
     * @param userId 用户ID
     * @return 最新的AI计划实体，如果不存在则返回null
     */
    AiPlan getLatestPlan(Long userId);

    /**
     * 删除AI健康计划
     * 
     * @param userId 用户ID
     * @param planId 计划ID
     */
    void deletePlan(Long userId, Long planId);

    /**
     * 更新计划内容（直接编辑模式）
     * 
     * @param userId 用户ID
     * @param planId 计划ID
     * @param planContent 新的计划内容JSON字符串
     * @return 更新后的计划实体
     */
    AiPlan updatePlanContent(Long userId, Long planId, String planContent);
}
