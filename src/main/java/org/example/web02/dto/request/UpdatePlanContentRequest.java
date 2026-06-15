package org.example.web02.dto.request;

/**
 * 更新计划内容请求 DTO
 *
 * 用于封装用户更新 AI 健康计划内容的请求参数，包含新的计划内容
 */
public class UpdatePlanContentRequest {

    /** 新的计划内容（JSON 格式） */
    private String planContent;

    /**
     * 获取计划内容
     *
     * @return 计划内容
     */
    public String getPlanContent() {
        return planContent;
    }

    /**
     * 设置计划内容
     *
     * @param planContent 计划内容
     */
    public void setPlanContent(String planContent) {
        this.planContent = planContent;
    }
}
