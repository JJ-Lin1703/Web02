package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.web02.entity.DictLabelOption;

import java.util.List;

@Mapper
public interface DictLabelOptionMapper {

    /** 查询某个分类下所有未删除的标签，按 sort 排序 */
    List<DictLabelOption> findByType(String type);

    /** 查询所有未删除的标签，按 type + sort 排序 */
    List<DictLabelOption> findAllActive();

    /** 查询所有未删除的标签（管理端全量） */
    List<DictLabelOption> findAll();

    /** 按 ID 查询（含已删除） */
    DictLabelOption findById(Long id);

    /** 新增 */
    int insert(DictLabelOption option);

    /** 更新 */
    int update(DictLabelOption option);

    /** 软删除 */
    int deleteById(Long id);
}