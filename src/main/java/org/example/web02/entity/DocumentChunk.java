package org.example.web02.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档块实体（向量RAG）
 */
@Data
public class DocumentChunk {

    private Long id;
    private String docId;
    private String docName;
    private Integer chunkIndex;
    private String chunkText;
    /** embedding向量，JSON数组字符串 */
    private String embedding;
    private LocalDateTime createTime;
}