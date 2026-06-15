package org.example.web02.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 健康档案请求 DTO
 *
 * 用于封装用户创建或更新健康档案的请求参数，包含用户的基本健康信息
 */
@Data
public class HealthRecordRequest {

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
}
