package org.example.web02.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 上传任务状态表
 * 追踪异步文档上传的解析进度
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadTask {
    private Long id;
    private String taskId;
    private String fileName;
    private Integer textLength;
    private String status;      // PENDING → PROCESSING → DONE / FAILED
    private String errorMsg;
    private String docId;
    private Integer retryCount;
    private Integer maxRetries;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}