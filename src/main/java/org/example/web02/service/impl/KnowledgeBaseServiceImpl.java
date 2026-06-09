package org.example.web02.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.web02.entity.DictLabelOption;
import org.example.web02.entity.KnowledgeBase;
import org.example.web02.entity.UserHealth;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.DictLabelOptionMapper;
import org.example.web02.mapper.KnowledgeBaseMapper;
import org.example.web02.mapper.UserHealthMapper;
import org.example.web02.service.DictLabelOptionService;
import org.example.web02.service.KnowledgeBaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签匹配型 RAG 知识检索服务
 * <p>
 * 所有合法标签值由 dict_label_options 表动态维护，不再硬编码。
 */
@Service
@Slf4j
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final UserHealthMapper userHealthMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final DictLabelOptionMapper dictLabelOptionMapper;
    private final DictLabelOptionService dictLabelOptionService;

    public KnowledgeBaseServiceImpl(UserHealthMapper userHealthMapper,
                                    KnowledgeBaseMapper knowledgeBaseMapper,
                                    DictLabelOptionMapper dictLabelOptionMapper,
                                    DictLabelOptionService dictLabelOptionService) {
        this.userHealthMapper = userHealthMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.dictLabelOptionMapper = dictLabelOptionMapper;
        this.dictLabelOptionService = dictLabelOptionService;
    }

    // ==================== RAG 检索 ====================

    @Override
    public List<KnowledgeBase> retrieveRelevantKnowledge(Long userId) {
        UserHealth health = userHealthMapper.findByUserId(userId);
        if (health == null) {
            log.warn("用户 {} 健康档案不存在，跳过 RAG 检索", userId);
            return Collections.emptyList();
        }

        Set<String> userTags = extractUserTags(health);
        if (userTags.isEmpty()) {
            log.info("用户 {} 无匹配标签，跳过 RAG 检索", userId);
            return Collections.emptyList();
        }

        log.info("用户 {} 提取到的 RAG 标签: {}", userId, userTags);

        List<String> tagList = new ArrayList<>(userTags);
        List<KnowledgeBase> results = knowledgeBaseMapper.findByTags(tagList);

        log.info("用户 {} RAG 检索命中 {} 条知识", userId, results.size());
        return results;
    }

    /**
     * 从 UserHealth 中提取标签，仅保留 dict_label_options 中存在的合法标签
     */
    private Set<String> extractUserTags(UserHealth health) {
        Set<String> tags = new HashSet<>();

        // 1. 健康目标（查表校验）
        Set<String> healthTargets = loadLabelNamesByType("health_target");
        addIfInSet(tags, health.getHealthTarget(), healthTargets);

        // 2. 饮食偏好（查表校验）
        Set<String> dietHobbies = loadLabelNamesByType("diet_hobby");
        addIfInSet(tags, health.getDietHobby(), dietHobbies);

        // 3. 过敏信息（逗号分隔，全部加入，由知识库标签决定能否匹配上）
        if (health.getAllergy() != null && !health.getAllergy().isBlank()) {
            for (String a : health.getAllergy().split("[,，]")) {
                String t = a.trim();
                if (!t.isEmpty()) tags.add(t);
            }
        }

        // 4. 慢性病史（扫描 dict 中的关键词）
        if (health.getMedicalHistory() != null && !health.getMedicalHistory().isBlank()) {
            String history = health.getMedicalHistory();
            for (String keyword : loadLabelNamesByType("medical_history")) {
                if (history.contains(keyword)) {
                    tags.add(keyword);
                }
            }
        }

        // 5. 活动水平（int → 按 sort 映射文本）
        Integer level = health.getActivityLevel();
        if (level != null) {
            List<DictLabelOption> activityOptions = dictLabelOptionMapper.findByType("activity_level");
            for (DictLabelOption opt : activityOptions) {
                if (level.equals(opt.getSort())) {
                    tags.add(opt.getLabelName());
                    break;
                }
            }
        }

        return tags;
    }

    /** 加载某分类下所有标签名（内存缓存，单次请求内复用） */
    private Set<String> loadLabelNamesByType(String type) {
        return dictLabelOptionMapper.findByType(type).stream()
                .map(DictLabelOption::getLabelName)
                .collect(Collectors.toSet());
    }

    // ==================== 管理端 CRUD ====================

    @Override
    @Transactional
    public KnowledgeBase create(KnowledgeBase kb) {
        validateTags(kb.getTags());
        kb.setCreateTime(new Date());
        kb.setUpdateTime(new Date());
        knowledgeBaseMapper.insert(kb);
        log.info("知识条目创建成功: id={}, title={}, tags={}", kb.getId(), kb.getTitle(), kb.getTags());
        return kb;
    }

    @Override
    @Transactional
    public KnowledgeBase update(Long id, KnowledgeBase kb) {
        KnowledgeBase existing = knowledgeBaseMapper.findById(id);
        if (existing == null) {
            throw new BusinessException("知识条目不存在");
        }
        validateTags(kb.getTags());
        kb.setId(id);
        kb.setUpdateTime(new Date());
        knowledgeBaseMapper.update(kb);
        log.info("知识条目更新成功: id={}", id);
        return getById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        KnowledgeBase existing = knowledgeBaseMapper.findById(id);
        if (existing == null) {
            throw new BusinessException("知识条目不存在");
        }
        knowledgeBaseMapper.deleteById(id);
        log.info("知识条目删除成功: id={}", id);
    }

    @Override
    public KnowledgeBase getById(Long id) {
        KnowledgeBase kb = knowledgeBaseMapper.findById(id);
        if (kb == null) {
            throw new BusinessException("知识条目不存在");
        }
        return kb;
    }

    @Override
    public List<KnowledgeBase> listAll() {
        return knowledgeBaseMapper.findAll();
    }

    @Override
    public Map<String, List<String>> getAllValidTags() {
        return dictLabelOptionService.getAllGrouped();
    }

    // ==================== 私有方法 ====================

    private void validateTags(String tags) {
        if (tags == null || tags.isBlank()) {
            throw new BusinessException("标签不能为空，请至少选择一个合法标签");
        }
        Set<String> allValid = dictLabelOptionService.getAllValidLabelNames();
        for (String tag : tags.split("[,，]")) {
            String t = tag.trim();
            if (t.isEmpty()) continue;
            if (!allValid.contains(t)) {
                throw new BusinessException("非法标签 '" + t + "'，不在标签规范中");
            }
        }
    }

    private void addIfInSet(Set<String> tags, String rawValue, Set<String> validSet) {
        if (rawValue == null || rawValue.isBlank()) return;
        String v = rawValue.trim();
        if (validSet.contains(v)) {
            tags.add(v);
        }
    }
}