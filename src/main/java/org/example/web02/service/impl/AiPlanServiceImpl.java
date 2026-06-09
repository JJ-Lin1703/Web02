package org.example.web02.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.web02.dto.request.TweakPlanRequest;
import org.example.web02.entity.AiPlan;
import org.example.web02.entity.UserHealth;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.AiPlanMapper;
import org.example.web02.mapper.UserHealthMapper;
import org.example.web02.service.AiPlanService;
import org.example.web02.service.DashScopeService;
import org.example.web02.service.KnowledgeBaseService;
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
    private final KnowledgeBaseService knowledgeBaseService;
    private final ObjectMapper objectMapper;

    public AiPlanServiceImpl(AiPlanMapper aiPlanMapper,
                             UserHealthMapper userHealthMapper,
                             DashScopeService dashScopeService,
                             KnowledgeBaseService knowledgeBaseService,
                             ObjectMapper objectMapper) {
        this.aiPlanMapper = aiPlanMapper;
        this.userHealthMapper = userHealthMapper;
        this.dashScopeService = dashScopeService;
        this.knowledgeBaseService = knowledgeBaseService;
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
    @Transactional
    public AiPlan tweakPlan(Long userId, TweakPlanRequest request) {
        AiPlan oldPlan = aiPlanMapper.findById(request.getPlanId());
        if (oldPlan == null || !oldPlan.getUserId().equals(userId)) {
            throw new BusinessException("计划不存在或无权修改");
        }

        UserHealth health = userHealthMapper.findByUserId(userId);
        if (health == null) {
            throw new BusinessException("请先完善健康档案信息");
        }

        // 构造微调 prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名专业营养师+持证健身教练。\n\n");

        // 重新跑 RAG 检索相关知识
        List<org.example.web02.entity.KnowledgeBase> ragKnowledge = knowledgeBaseService.retrieveRelevantKnowledge(userId);
        if (ragKnowledge != null && !ragKnowledge.isEmpty()) {
            prompt.append("【权威健康知识库参考】\n");
            for (int i = 0; i < ragKnowledge.size(); i++) {
                org.example.web02.entity.KnowledgeBase kb = ragKnowledge.get(i);
                prompt.append((i + 1)).append(". ").append(kb.getTitle()).append("\n");
                prompt.append(kb.getContent()).append("\n\n");
            }
        }

        prompt.append("以下是一份已生成好的完整健康计划JSON。用户对其中某一部分不满意，要求仅修改这一小部分。\n\n");

        prompt.append("【用户反馈】\n");
        prompt.append("我对【").append(request.getDayName()).append("】的【").append(request.getModule()).append("】不满意");
        if (request.getItemDesc() != null && !request.getItemDesc().isBlank()) {
            prompt.append("，具体是【").append(request.getItemDesc()).append("】");
        }
        prompt.append("。\n");
        if (request.getReason() != null && !request.getReason().isBlank()) {
            prompt.append("原因：").append(request.getReason()).append("\n");
        }
        prompt.append("\n");

        prompt.append("【修改要求 - 必须严格遵守】\n");
        prompt.append("1. 严格只修改【").append(request.getDayName()).append("】的【").append(request.getModule()).append("】部分\n");
        prompt.append("2. 如果你修改了饮食，请参考知识库中的饮食禁忌和健康建议来替换合适的食物\n");
        prompt.append("3. 该日其他模块、其他日期的所有内容完全不动（一个字、一个标点都不改）\n");
        prompt.append("4. 以完整JSON格式返回，结构必须与原计划完全一致：{\"summary\": {...}, \"weeklyPlan\": [...]}\n");
        prompt.append("5. 不要添加 planTitle、totalCalorie 等额外字段\n\n");

        prompt.append("【原计划JSON】\n");
        prompt.append(oldPlan.getPlanContent());
        prompt.append("\n\n");
        prompt.append("请输出完整的修改后 JSON，不要省略任何内容，不要加任何解释：");

        String response = dashScopeService.generateText(prompt.toString(), 8000);
        String jsonStr = extractPlanJsonFromResponse(response);

        if (jsonStr == null || jsonStr.isEmpty()) {
            throw new BusinessException("AI微调失败：无法解析返回的JSON");
        }

        AiPlan latestPlan = aiPlanMapper.findLatestByUserId(userId);
        int versionNo = (latestPlan != null) ? latestPlan.getVersionNo() + 1 : 1;
        Long parentPlanId = (latestPlan != null) ? latestPlan.getId() : null;

        AiPlan newPlan = new AiPlan();
        newPlan.setUserId(userId);
        newPlan.setPlanTitle(oldPlan.getPlanTitle());
        newPlan.setTotalCalorie(oldPlan.getTotalCalorie());
        newPlan.setPlanContent(jsonStr);
        newPlan.setVersionNo(versionNo);
        newPlan.setParentPlanId(parentPlanId);
        newPlan.setGenerateTime(new Date());
        newPlan.setUpdateTime(new Date());

        aiPlanMapper.insert(newPlan);
        return newPlan;
    }

    private String extractPlanJsonFromResponse(String response) {
        if (response == null) return null;
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        return null;
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
