package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.KnowledgeBase;

import java.util.List;

/**
 * 健康知识库数据访问接口
 *
 * 提供健康知识库表的 CRUD 操作，包括查询知识条目、
 * 根据标签匹配知识、插入、更新、删除等功能
 */
@Mapper
public interface KnowledgeBaseMapper {

    /**
     * 根据标签列表模糊匹配知识条目
     * 用户标签与知识库 tags 字段做交集匹配，每个用户标签作为独立关键词去 LIKE 匹配
     *
     * @param tags 标签列表
     * @return 匹配的知识条目列表
     */
    List<KnowledgeBase> findByTags(@Param("tags") List<String> tags);

    /**
     * 插入知识条目
     *
     * @param knowledgeBase 知识条目实体
     * @return 影响行数
     */
    int insert(KnowledgeBase knowledgeBase);

    /**
     * 更新知识条目
     *
     * @param knowledgeBase 知识条目实体
     * @return 影响行数
     */
    int update(KnowledgeBase knowledgeBase);

    /**
     * 根据知识条目 ID 查询知识条目
     *
     * @param id 知识条目 ID
     * @return 知识条目实体
     */
    KnowledgeBase findById(@Param("id") Long id);

    /**
     * 查询所有知识条目
     *
     * @return 知识条目列表
     */
    List<KnowledgeBase> findAll();

    /**
     * 删除知识条目（软删除）
     *
     * @param id 知识条目 ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}