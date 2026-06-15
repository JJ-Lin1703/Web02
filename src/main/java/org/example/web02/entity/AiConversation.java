package org.example.web02.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 会话实体类
 *
 * 对应数据库表 ai_conversation，存储用户与 AI 的对话记录，
 * 包括会话 ID、问题、回答、是否基于文档生成等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiConversation {

    /** 会话记录 ID（主键） */
    private Long id;

    /** 用户 ID（外键，关联 user 表） */
    private Long userId;

    /** 会话 ID（用于关联同一会话的多轮对话） */
    private String sessionId;

    /** 用户提问内容 */
    private String question;

    /** AI 回答内容 */
    private String answer;

    /** 是否基于文档生成（0-否，1-是） */
    private Integer docBased;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 是否删除（0-未删除，1-已删除，软删除标记） */
    private Integer isDeleted;
}