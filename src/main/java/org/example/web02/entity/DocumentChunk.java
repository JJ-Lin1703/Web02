package org.example.web02.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档块实体类
 *
 * 对应数据库表 document_chunk，存储文档分块后的文本片段及其向量嵌入，
 * 用于向量 RAG 检索
 */
@Data
public class DocumentChunk {

    /** 文档块 ID（主键） */
    private Long id;

    /** 文档 ID（关联 upload_task 表的 docId） */
    private String docId;

    /** 文档名称 */
    private String docName;

    /** 文档块索引（分块顺序） */
    private Integer chunkIndex;

    /** 文档块文本内容 */
    private String chunkText;

    /** 向量嵌入（JSON 数组字符串，用于向量检索） */
    private String embedding;

    /** 创建时间 */
    private LocalDateTime createTime;
}