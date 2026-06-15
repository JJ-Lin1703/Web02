package org.example.web02.dto.request;

import lombok.Data;

/**
 * 调整计划请求 DTO
 *
 * 用于封装用户调整 AI 健康计划的请求参数，包含计划 ID、星期、模块、项目和调整原因
 */
@Data
public class TweakPlanRequest {

    /** 健康计划 ID */
    private Long planId;

    /** 星期名称（如：周一、周二等） */
    private String dayName;

    /** 模块名称（如：饮食、运动等） */
    private String module;

    /** 项目描述 */
    private String itemDesc;

    /** 调整原因 */
    private String reason;
}