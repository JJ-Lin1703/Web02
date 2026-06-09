package org.example.web02.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.web02.entity.AiPlan;
import org.example.web02.entity.UserHealth;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.AiPlanMapper;
import org.example.web02.mapper.UserHealthMapper;
import org.example.web02.service.AiPlanService;
import org.example.web02.service.DashScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AiPlanServiceImpl implements AiPlanService {

    private final AiPlanMapper aiPlanMapper;
    private final UserHealthMapper userHealthMapper;
    private final DashScopeService dashScopeService;
    private final ObjectMapper objectMapper;

    public AiPlanServiceImpl(AiPlanMapper aiPlanMapper,
                             UserHealthMapper userHealthMapper,
                             DashScopeService dashScopeService,
                             ObjectMapper objectMapper) {
        this.aiPlanMapper = aiPlanMapper;
        this.userHealthMapper = userHealthMapper;
        this.dashScopeService = dashScopeService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AiPlan generatePlan(Long userId) {
        UserHealth health = userHealthMapper.findByUserId(userId);
        if (health == null) {
            throw new BusinessException("请先完善健康档案信息");
        }

        Map<String, Object> planResult = dashScopeService.generateHealthPlan(userId);

        AiPlan latestPlan = aiPlanMapper.findLatestByUserId(userId);
        int versionNo = (latestPlan != null) ? latestPlan.getVersionNo() + 1 : 1;
        Long parentPlanId = (latestPlan != null) ? latestPlan.getId() : null;

        AiPlan plan = new AiPlan();
        plan.setUserId(userId);
        plan.setPlanTitle((String) planResult.get("planTitle"));
        Object calorieObj = planResult.get("totalCalorie");
        if (calorieObj instanceof Number) {
            plan.setTotalCalorie(((Number) calorieObj).intValue());
        } else {
            plan.setTotalCalorie(health.getTdee() != null ? health.getTdee().intValue() : 2000);
        }
        plan.setPlanContent((String) planResult.get("planContent"));
        plan.setVersionNo(versionNo);
        plan.setParentPlanId(parentPlanId);
        plan.setGenerateTime(new Date());
        plan.setUpdateTime(new Date());

        aiPlanMapper.insert(plan);
        return plan;
    }

    @Override
    public List<AiPlan> getPlanHistory(Long userId) {
        return aiPlanMapper.findByUserId(userId);
    }

    @Override
    public AiPlan getLatestPlan(Long userId) {
        return aiPlanMapper.findLatestByUserId(userId);
    }

    @Override
    @Transactional
    public void deletePlan(Long userId, Long planId) {
        AiPlan plan = aiPlanMapper.findById(planId);
        if (plan == null) {
            throw new BusinessException("计划不存在");
        }
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此计划");
        }
        aiPlanMapper.deleteById(planId);
    }
}
