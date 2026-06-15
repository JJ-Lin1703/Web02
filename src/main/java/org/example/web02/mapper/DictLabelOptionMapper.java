package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.web02.entity.DictLabelOption;

import java.util.List;

/**
 * 标签字典选项数据访问接口
 *
 * 提供标签字典选项表的 CRUD 操作，包括查询标签、
 * 插入标签、更新标签、删除标签等功能
 */
@Mapper
public interface DictLabelOptionMapper {

    /**
     * 查询某个分类下所有未删除的标签，按 sort 排序
     *
     * @param type 标签分类
     * @return 标签选项列表
     */
    List<DictLabelOption> findByType(String type);

    /**
     * 查询所有未删除的标签，按 type + sort 排序
     *
     * @return 标签选项列表
     */
    List<DictLabelOption> findAllActive();

    /**
     * 查询所有未删除的标签（管理端全量）
     *
     * @return 标签选项列表
     */
    List<DictLabelOption> findAll();

    /**
     * 按 ID 查询（含已删除）
     *
     * @param id 标签选项 ID
     * @return 标签选项实体
     */
    DictLabelOption findById(Long id);

    /**
     * 新增标签选项
     *
     * @param option 标签选项实体
     * @return 影响行数
     */
    int insert(DictLabelOption option);

    /**
     * 更新标签选项
     *
     * @param option 标签选项实体
     * @return 影响行数
     */
    int update(DictLabelOption option);

    /**
     * 软删除标签选项
     *
     * @param id 标签选项 ID
     * @return 影响行数
     */
    int deleteById(Long id);
}