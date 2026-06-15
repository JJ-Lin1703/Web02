package org.example.web02.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 上传任务实体类
 *
 * 对应数据库表 upload_task，追踪异步文档上传的解析进度，
 * 包括任务 ID、文件名、状态、错误信息、重试次数等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadTask {

    /** 任务记录 ID（主键） */
    private Long id;

    /** 任务 ID（唯一标识，用于查询任务状态） */
    private String taskId;

    /** 文件名 */
    private String fileName;

    /** 文本长度（字符数） */
    private Integer textLength;

    /** 任务状态：PENDING → PROCESSING → DONE / FAILED */
    private String status;

    /** 错误信息（任务失败时的错误描述） */
    private String errorMsg;

    /** 文档 ID（向量化入库后的文档标识） */
    private String docId;

    /** 重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetries;

    /** 用户 ID（外键，关联 user 表） */
    private Long userId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}