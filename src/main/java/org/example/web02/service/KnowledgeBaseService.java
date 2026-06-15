package org.example.web02.service;

import org.example.web02.entity.KnowledgeBase;

import java.util.List;
import java.util.Map;

/**
 * 知识库服务接口
 * 提供健康知识的检索、管理和标签校验功能
 */
public interface KnowledgeBaseService {

    /**
     * 根据用户健康档案提取标签，从知识库中检索匹配的健康知识
     * 
     * @param userId 用户ID
     * @return 匹配的健康知识列表
     */
    List<KnowledgeBase> retrieveRelevantKnowledge(Long userId);

    // ======================== 管理端 CRUD ===========================

    /**
     * 创建知识条目
     * 
     * @param knowledgeBase 知识条目实体
     * @return 创建后的知识条目
     */
    KnowledgeBase create(KnowledgeBase knowledgeBase);

    /**
     * 更新知识条目
     * 
     * @param id 知识条目ID
     * @param knowledgeBase 更新内容
     * @return 更新后的知识条目
     */
    KnowledgeBase update(Long id, KnowledgeBase knowledgeBase);

    /**
     * 删除知识条目
     * 
     * @param id 知识条目ID
     */
    void delete(Long id);

    /**
     * 根据ID获取知识条目
     * 
     * @param id 知识条目ID
     * @return 知识条目实体，如果不存在则返回null
     */
    KnowledgeBase getById(Long id);

    /**
     * 获取所有知识条目列表
     * 
     * @return 知识条目列表
     */
    List<KnowledgeBase> listAll();

    /**
     * 获取合法标签池（分组），供管理端下拉框使用
     * 
     * @return 标签分组Map，格式：{"健康目标": ["减肥","增肌","维持健康"], "饮食偏好": [...], ...}
     */
    Map<String, List<String>> getAllValidTags();
}