package org.example.web02.service;

import org.example.web02.dto.request.TweakPlanRequest;
import org.example.web02.entity.AiPlan;

import java.util.List;

public interface AiPlanService {

    AiPlan generatePlan(Long userId);

    AiPlan tweakPlan(Long userId, TweakPlanRequest request);

    List<AiPlan> getPlanHistory(Long userId);

    AiPlan getLatestPlan(Long userId);

    void deletePlan(Long userId, Long planId);
}
