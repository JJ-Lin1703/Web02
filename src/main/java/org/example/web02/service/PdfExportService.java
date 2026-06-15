package org.example.web02.service;

import org.example.web02.entity.AiPlan;

/**
 * PDF导出服务接口
 * 提供健康计划的PDF文档导出功能
 */
public interface PdfExportService {

    /**
     * 导出健康计划为PDF文档
     * 
     * @param userId 用户ID
     * @param plan AI健康计划实体
     * @return PDF文件的字节数组
     */
    byte[] exportHealthPlanToPdf(Long userId, AiPlan plan);
}