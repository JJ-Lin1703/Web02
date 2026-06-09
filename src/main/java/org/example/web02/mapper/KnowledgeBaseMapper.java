package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.KnowledgeBase;

import java.util.List;

@Mapper
public interface KnowledgeBaseMapper {

    /**
     * 根据标签列表模糊匹配知识条目
     * 用户标签与知识库 tags 字段做交集匹配，每个用户标签作为独立关键词去 LIKE 匹配
     */
    List<KnowledgeBase> findByTags(@Param("tags") List<String> tags);

    int insert(KnowledgeBase knowledgeBase);

    int update(KnowledgeBase knowledgeBase);

    KnowledgeBase findById(@Param("id") Long id);

    List<KnowledgeBase> findAll();

    int deleteById(@Param("id") Long id);
}