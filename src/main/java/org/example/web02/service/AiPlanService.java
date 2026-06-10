package org.example.web02.service;

import org.example.web02.dto.request.TweakPlanRequest;
import org.example.web02.dto.response.PageResult;
import org.example.web02.entity.AiPlan;

import java.util.List;

public interface AiPlanService {

    AiPlan generatePlan(Long userId);

    AiPlan generatePlan(Long userId, String apiKey);

    AiPlan tweakPlan(Long userId, TweakPlanRequest request);

    AiPlan tweakPlan(Long userId, TweakPlanRequest request, String apiKey);

    List<AiPlan> getPlanHistory(Long userId);

    PageResult<AiPlan> getPlanHistoryPaginated(Long userId, int pageNum, int pageSize);

    AiPlan getLatestPlan(Long userId);

    void deletePlan(Long userId, Long planId);

    AiPlan updatePlanContent(Long userId, Long planId, String planContent);
}
