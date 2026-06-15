package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.DocumentChunk;

import java.util.List;

/**
 * 文档块数据访问接口
 *
 * 提供文档块表的 CRUD 操作，包括插入文档块、批量插入、
 * 查询文档块、删除文档块等功能，用于向量 RAG 检索
 */
@Mapper
public interface DocumentChunkMapper {

    /**
     * 插入单个文档块
     *
     * @param chunk 文档块实体
     */
    void insert(DocumentChunk chunk);

    /**
     * 批量插入文档块
     *
     * @param list 文档块列表
     */
    void insertBatch(@Param("list") List<DocumentChunk> list);

    /**
     * 根据文档 ID 查询文档块
     *
     * @param docId 文档 ID
     * @return 文档块列表
     */
    List<DocumentChunk> findByDocId(@Param("docId") String docId);

    /**
     * 查询所有文档块
     *
     * @return 文档块列表
     */
    List<DocumentChunk> findAll();

    /**
     * 查询所有不重复的文档名称
     *
     * @return 文档名称列表
     */
    List<String> findDistinctDocs();

    /**
     * 根据文档 ID 删除文档块
     *
     * @param docId 文档 ID
     */
    void deleteByDocId(@Param("docId") String docId);
}