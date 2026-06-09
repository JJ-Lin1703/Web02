package org.example.web02.service;

import org.example.web02.entity.DictLabelOption;

import java.util.List;
import java.util.Map;

public interface DictLabelOptionService {

    /** 查询某个分类下所有标签名（有序） */
    List<String> getLabelNamesByType(String type);

    /** 查询所有标签，按分类分组 */
    Map<String, List<String>> getAllGrouped();

    /** 获取所有合法标签的并集（用于校验） */
    java.util.Set<String> getAllValidLabelNames();

    // ====== 管理端 CRUD ======

    /** 查询所有未删除的标签（管理端全量） */
    List<DictLabelOption> listAll();

    /** 按 ID 查询 */
    DictLabelOption getById(Long id);

    /** 新增标签 */
    DictLabelOption create(DictLabelOption option);

    /** 修改标签 */
    DictLabelOption update(Long id, DictLabelOption option);

    /** 软删除 */
    void delete(Long id);
}