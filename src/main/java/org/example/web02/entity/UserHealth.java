package org.example.web02.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户健康档案实体类
 *
 * 对应数据库表 user_health，存储用户的健康档案信息，
 * 包括年龄、性别、身高、体重、活动水平、饮食爱好、健康目标、
 * 过敏史、病史以及计算得出的 BMR、TDEE、BMI 等指标
 */
@Data
public class UserHealth {

    /** 健康档案 ID（主键） */
    private Long id;

    /** 用户 ID（外键，关联 user 表） */
    private Long userId;

    /** 年龄 */
    private Integer age;

    /** 性别（0-女，1-男） */
    private Integer gender;

    /** 身高（厘米） */
    private BigDecimal height;

    /** 体重（千克） */
    private BigDecimal weight;

    /** 活动水平（1-久坐，2-轻度活动，3-中度活动，4-高度活动，5-极度活动） */
    private Integer activityLevel;

    /** 饮食爱好 */
    private String dietHobby;

    /** 健康目标 */
    private String healthTarget;

    /** 过敏史 */
    private String allergy;

    /** 病史 */
    private String medicalHistory;

    /** 基础代谢率（BMR，千卡/天） */
    private BigDecimal bmr;

    /** 每日总能量消耗（TDEE，千卡/天） */
    private BigDecimal tdee;

    /** 身体质量指数（BMI） */
    private BigDecimal bmi;

    /** 更新时间 */
    private Date updateTime;

    /** 创建时间 */
    private Date createTime;

    /** 是否删除（0-未删除，1-已删除，软删除标记） */
    private Integer isDeleted;
}
