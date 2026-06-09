package org.example.web02.service;

import org.example.web02.entity.KnowledgeBase;

import java.util.List;
import java.util.Map;

public interface KnowledgeBaseService {

    /**
     * 根据用户健康档案提取标签，从知识库中检索匹配的健康知识
     */
    List<KnowledgeBase> retrieveRelevantKnowledge(Long userId);

    // ======================== 管理端 CRUD ===========================

    KnowledgeBase create(KnowledgeBase knowledgeBase);

    KnowledgeBase update(Long id, KnowledgeBase knowledgeBase);

    void delete(Long id);

    KnowledgeBase getById(Long id);

    List<KnowledgeBase> listAll();

    /**
     * 获取合法标签池（分组），供管理端下拉框使用
     * 返回格式：{"健康目标": ["减肥","增肌","维持健康"], "饮食偏好": [...], ...}
     */
    Map<String, List<String>> getAllValidTags();
}