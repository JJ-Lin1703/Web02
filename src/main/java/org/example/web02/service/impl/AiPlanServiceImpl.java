package org.example.web02.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.web02.dto.request.TweakPlanRequest;
import org.example.web02.dto.response.PageResult;
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

/**
 * AI健康计划服务实现类
 * 负责AI计划的生成、微调、版本管理和CRUD操作
 * 核心流程：用户健康档案 → RAG知识检索 → 大模型生成 → 计划持久化
 */
@Service
public class AiPlanServiceImpl implements AiPlanService {

    /** AI计划Mapper */
    private final AiPlanMapper aiPlanMapper;
    /** 用户健康档案Mapper */
    private final UserHealthMapper userHealthMapper;
    /** 阿里百炼大模型服务 */
    private final DashScopeService dashScopeService;
    /** RAG知识库服务 */
    private final KnowledgeBaseService knowledgeBaseService;
    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入依赖
     */
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

    /**
     * 生成AI健康计划（无API Key版本）
     * @param userId 用户ID
     * @return AI计划实体
     */
    @Override
    @Transactional
    public AiPlan generatePlan(Long userId) {
        return generatePlan(userId, null);
    }

    /**
     * 生成AI健康计划（核心方法）
     * 流程：验证健康档案 → 调用DashScope生成计划 → 构建版本信息 → 持久化
     * @param userId 用户ID
     * @param apiKey 用户传入的API Key（可选）
     * @return AI计划实体
     */
    @Transactional
    public AiPlan generatePlan(Long userId, String apiKey) {
        // Step 1: 验证用户健康档案是否存在
        UserHealth health = userHealthMapper.findByUserId(userId);
        if (health == null) {
            throw new BusinessException("请先完善健康档案信息");
        }

        // Step 2: 调用DashScope大模型生成健康计划（内部已包含RAG检索）
        Map<String, Object> planResult = dashScopeService.generateHealthPlan(userId, apiKey);

        // Step 3: 计算版本号（实现版本链）
        AiPlan latestPlan = aiPlanMapper.findLatestByUserId(userId);
        int versionNo = (latestPlan != null) ? latestPlan.getVersionNo() + 1 : 1;
        Long parentPlanId = (latestPlan != null) ? latestPlan.getId() : null;

        // Step 4: 构建AiPlan实体
        AiPlan plan = new AiPlan();
        plan.setUserId(userId);
        plan.setPlanTitle((String) planResult.get("planTitle"));
        
        // 安全转换总热量
        Object calorieObj = planResult.get("totalCalorie");
        if (calorieObj instanceof Number) {
            plan.setTotalCalorie(((Number) calorieObj).intValue());
        } else {
            plan.setTotalCalorie(health.getTdee() != null ? health.getTdee().intValue() : 2000);
        }
        
        plan.setPlanContent((String) planResult.get("planContent"));
        plan.setVersionNo(versionNo);
        plan.setParentPlanId(parentPlanId);  // 建立版本继承关系
        plan.setGenerateTime(new Date());
        plan.setUpdateTime(new Date());

        // Step 5: 持久化到数据库
        aiPlanMapper.insert(plan);
        return plan;
    }

    /**
     * 微调AI健康计划（无API Key版本）
     * @param userId 用户ID
     * @param request 微调请求（包含要修改的日期、模块、原因等）
     * @return 修改后的新计划
     */
    @Override
    @Transactional
    public AiPlan tweakPlan(Long userId, TweakPlanRequest request) {
        return tweakPlan(userId, request, null);
    }

    /**
     * 微调AI健康计划（核心方法）
     * 支持用户对已有计划的特定部分进行修改，保持其他内容不变
     * 流程：验证权限 → 构建微调Prompt（含RAG知识）→ 调用大模型 → 创建新版本计划
     * @param userId 用户ID
     * @param request 微调请求（包含要修改的日期、模块、原因等）
     * @param apiKey 用户传入的API Key（可选）
     * @return 修改后的新计划
     */
    @Override
    @Transactional
    public AiPlan tweakPlan(Long userId, TweakPlanRequest request, String apiKey) {
        // Step 1: 验证原计划存在且用户有权限
        AiPlan oldPlan = aiPlanMapper.findById(request.getPlanId());
        if (oldPlan == null || !oldPlan.getUserId().equals(userId)) {
            throw new BusinessException("计划不存在或无权修改");
        }

        // Step 2: 验证用户健康档案存在
        UserHealth health = userHealthMapper.findByUserId(userId);
        if (health == null) {
            throw new BusinessException("请先完善健康档案信息");
        }

        // Step 3: 构建微调Prompt（核心：保留原计划，仅修改指定部分）
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名专业营养师+持证健身教练。\n\n");

        // 添加RAG知识库参考
        List<org.example.web02.entity.KnowledgeBase> ragKnowledge = knowledgeBaseService.retrieveRelevantKnowledge(userId);
        if (ragKnowledge != null && !ragKnowledge.isEmpty()) {
            prompt.append("【权威健康知识库参考】\n");
            for (int i = 0; i < ragKnowledge.size(); i++) {
                org.example.web02.entity.KnowledgeBase kb = ragKnowledge.get(i);
                prompt.append((i + 1)).append(". ").append(kb.getTitle()).append("\n");
                prompt.append(kb.getContent()).append("\n\n");
            }
        }

        // 用户反馈信息
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

        // 严格的修改规则（确保只修改指定部分）
        prompt.append("【修改要求 - 必须严格遵守】\n");
        prompt.append("1. 严格只修改【").append(request.getDayName()).append("】的【").append(request.getModule()).append("】部分\n");
        prompt.append("2. 如果你修改了饮食，请参考知识库中的饮食禁忌和健康建议来替换合适的食物\n");
        prompt.append("3. 该日其他模块、其他日期的所有内容完全不动（一个字、一个标点都不改）\n");
        prompt.append("4. 以完整JSON格式返回，结构必须与原计划完全一致：{\"summary\": {...}, \"weeklyPlan\": [...]}\n");
        prompt.append("5. 不要添加 planTitle、totalCalorie 等额外字段\n\n");

        // 原计划JSON（作为修改基础）
        prompt.append("【原计划JSON】\n");
        prompt.append(oldPlan.getPlanContent());
        prompt.append("\n\n");
        prompt.append("请输出完整的修改后 JSON，不要省略任何内容，不要加任何解释：");

        // Step 4: 调用大模型进行微调（使用更大的token限制8000）
        String jsonStr = null;
        try {
            String response = dashScopeService.generateText(prompt.toString(), 8000, apiKey);
            jsonStr = extractPlanJsonFromResponse(response);
        } catch (Exception e) {
            throw new BusinessException("AI服务调用失败：" + e.getMessage());
        }

        if (jsonStr == null || jsonStr.isEmpty()) {
            throw new BusinessException("AI微调失败：无法解析返回的JSON");
        }

        // Step 5: 创建新版本计划（版本链继承）
        AiPlan latestPlan = aiPlanMapper.findLatestByUserId(userId);
        int versionNo = (latestPlan != null) ? latestPlan.getVersionNo() + 1 : 1;
        Long parentPlanId = (latestPlan != null) ? latestPlan.getId() : null;

        AiPlan newPlan = new AiPlan();
        newPlan.setUserId(userId);
        newPlan.setPlanTitle(oldPlan.getPlanTitle());
        newPlan.setTotalCalorie(oldPlan.getTotalCalorie());  // 保持原热量不变
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
    public PageResult<AiPlan> getPlanHistoryPaginated(Long userId, int pageNum, int pageSize) {
        long offset = (long) (pageNum - 1) * pageSize;
        List<AiPlan> records = aiPlanMapper.findByUserIdPaginated(userId, offset, pageSize);
        long total = aiPlanMapper.countByUserId(userId);
        return PageResult.of(records, total, pageNum, pageSize);
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

    @Override
    @Transactional
    public AiPlan updatePlanContent(Long userId, Long planId, String planContent) {
        AiPlan plan = aiPlanMapper.findById(planId);
        if (plan == null) {
            throw new BusinessException("计划不存在");
        }
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此计划");
        }
        
        plan.setPlanContent(planContent);
        plan.setUpdateTime(new Date());
        aiPlanMapper.updateById(plan);
        
        return plan;
    }
}
