-- upload_task 表：追踪异步文档上传的解析进度
-- 请执行此 SQL 创建表

CREATE TABLE IF NOT EXISTS upload_task (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    task_id     VARCHAR(64)  NOT NULL UNIQUE COMMENT '任务唯一标识（UUID）',
    file_name   VARCHAR(255) NOT NULL COMMENT '文件名',
    text_length INT                   COMMENT '文本长度',
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PROCESSING/DONE/FAILED',
    error_msg   TEXT                  COMMENT '错误信息',
    doc_id      VARCHAR(64)           COMMENT '入库后的文档ID',
    retry_count INT          NOT NULL DEFAULT 0 COMMENT '已重试次数',
    max_retries INT          NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    user_id     BIGINT                COMMENT '上传用户ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_status (status),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传任务状态表';