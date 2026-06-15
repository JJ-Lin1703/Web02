package org.example.web02.service;

import org.example.web02.entity.DictLabelOption;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 标签选项服务接口
 * 提供健康档案标签字典的查询和管理功能
 */
public interface DictLabelOptionService {

    /**
     * 查询某个分类下所有标签名（有序）
     * 
     * @param type 分类类型
     * @return 标签名称列表（按排序字段排序）
     */
    List<String> getLabelNamesByType(String type);

    /**
     * 查询所有标签，按分类分组
     * 
     * @return 标签分组Map（类型中文名 -> 标签名称列表）
     */
    Map<String, List<String>> getAllGrouped();

    /**
     * 获取所有合法标签的并集（用于校验）
     * 
     * @return 所有合法标签名称的集合
     */
    Set<String> getAllValidLabelNames();

    // ====== 管理端 CRUD ======

    /**
     * 查询所有未删除的标签（管理端全量）
     * 
     * @return 标签选项列表
     */
    List<DictLabelOption> listAll();

    /**
     * 按 ID 查询标签选项
     * 
     * @param id 标签选项ID
     * @return 标签选项实体，如果不存在则返回null
     */
    DictLabelOption getById(Long id);

    /**
     * 新增标签选项
     * 
     * @param option 标签选项实体
     * @return 创建后的标签选项
     */
    DictLabelOption create(DictLabelOption option);

    /**
     * 修改标签选项
     * 
     * @param id 标签选项ID
     * @param option 更新内容
     * @return 更新后的标签选项
     */
    DictLabelOption update(Long id, DictLabelOption option);

    /**
     * 软删除标签选项
     * 
     * @param id 标签选项ID
     */
    void delete(Long id);
}