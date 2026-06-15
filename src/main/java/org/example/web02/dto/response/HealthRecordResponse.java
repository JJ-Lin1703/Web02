package org.example.web02.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 健康档案响应 DTO
 *
 * 用于封装用户健康档案的返回结果，包含用户的基本健康信息、
 * 计算得出的 BMR、TDEE、BMI 等指标以及创建和更新时间
 */
@Data
@Builder
public class HealthRecordResponse {

    /** 健康档案 ID */
    private Long id;

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

    /** 基础代谢率（BMR） */
    private BigDecimal bmr;

    /** 每日总能量消耗（TDEE） */
    private BigDecimal tdee;

    /** 身体质量指数（BMI） */
    private BigDecimal bmi;

    /** 更新时间 */
    private Date updateTime;

    /** 创建时间 */
    private Date createTime;
}
