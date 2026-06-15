package org.example.web02.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 打卡记录实体类
 *
 * 对应数据库表 clock_record，存储用户对 AI 健康计划的打卡记录，
 * 包括计划 ID、记录日期、完成项、总项数、未完成原因、完成率等
 */
@Data
public class ClockRecord {

    /** 打卡记录 ID（主键） */
    private Long id;

    /** 用户 ID（外键，关联 user 表） */
    private Long userId;

    /** 计划 ID（外键，关联 ai_plan 表） */
    private Long planId;

    /** 记录日期 */
    private Date recordDate;

    /** 已完成项（JSON 格式，记录已完成的饮食或运动项） */
    private String finishItem;

    /** 总项数（JSON 格式，记录计划中的所有项） */
    private String totalItem;

    /** 未完成原因（可选） */
    private String unfinishReason;

    /** 完成率（百分比） */
    private BigDecimal finishRate;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 是否删除（0-未删除，1-已删除，软删除标记） */
    private Integer isDeleted;
}