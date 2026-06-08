package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.entity.AiPlan;
import org.example.web02.service.AiPlanService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-plan")
public class AiPlanController {

    private final AiPlanService aiPlanService;

    public AiPlanController(AiPlanService aiPlanService) {
        this.aiPlanService = aiPlanService;
    }

    @PostMapping("/generate")
    public ApiResponse<AiPlan> generatePlan(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AiPlan plan = aiPlanService.generatePlan(userId);
        return ApiResponse.success("AI计划生成成功", plan);
    }

    @GetMapping("/history")
    public ApiResponse<List<AiPlan>> getPlanHistory(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<AiPlan> history = aiPlanService.getPlanHistory(userId);
        return ApiResponse.success(history);
    }

    @GetMapping("/latest")
    public ApiResponse<AiPlan> getLatestPlan(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AiPlan plan = aiPlanService.getLatestPlan(userId);
        return ApiResponse.success(plan);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePlan(Authentication authentication,
                                         @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        aiPlanService.deletePlan(userId, id);
        return ApiResponse.success("删除成功");
    }
}
