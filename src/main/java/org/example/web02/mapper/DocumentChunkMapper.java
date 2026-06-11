package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.DocumentChunk;

import java.util.List;

@Mapper
public interface DocumentChunkMapper {

    void insert(DocumentChunk chunk);

    void insertBatch(@Param("list") List<DocumentChunk> list);

    List<DocumentChunk> findByDocId(@Param("docId") String docId);

    List<DocumentChunk> findAll();

    List<String> findDistinctDocs();

    void deleteByDocId(@Param("docId") String docId);
}