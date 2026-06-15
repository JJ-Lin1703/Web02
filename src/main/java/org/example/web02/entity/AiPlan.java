package org.example.web02.entity;

import lombok.Data;

import java.util.Date;

/**
 * AI 健康计划实体类
 *
 * 对应数据库表 ai_plan，存储 AI 生成的健康计划信息，
 * 包括计划标题、总热量、计划内容、版本号、父计划 ID 等
 */
@Data
public class AiPlan {

    /** 计划 ID（主键） */
    private Long id;

    /** 用户 ID（外键，关联 user 表） */
    private Long userId;

    /** 计划标题 */
    private String planTitle;

    /** 总热量（千卡） */
    private Integer totalCalorie;

    /** 计划内容（JSON 格式，包含每日饮食和运动安排） */
    private String planContent;

    /** 版本号（用于追踪计划调整历史） */
    private Integer versionNo;

    /** 父计划 ID（调整后的计划关联原计划） */
    private Long parentPlanId;

    /** 生成时间 */
    private Date generateTime;

    /** 更新时间 */
    private Date updateTime;

    /** 是否删除（0-未删除，1-已删除，软删除标记） */
    private Integer isDeleted;
}
