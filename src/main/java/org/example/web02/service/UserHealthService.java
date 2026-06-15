package org.example.web02.service;

import org.example.web02.dto.request.HealthRecordRequest;
import org.example.web02.dto.response.HealthRecordResponse;

/**
 * 用户健康档案服务接口
 * 提供用户健康档案的创建、更新和查询功能
 */
public interface UserHealthService {

    /**
     * 创建用户健康档案
     * 
     * @param userId 用户ID
     * @param request 健康档案请求DTO
     * @return 健康档案响应DTO
     */
    HealthRecordResponse createHealthRecord(Long userId, HealthRecordRequest request);

    /**
     * 更新用户健康档案
     * 
     * @param userId 用户ID
     * @param request 健康档案请求DTO
     * @return 健康档案响应DTO
     */
    HealthRecordResponse updateHealthRecord(Long userId, HealthRecordRequest request);

    /**
     * 获取用户健康档案
     * 
     * @param userId 用户ID
     * @return 健康档案响应DTO
     */
    HealthRecordResponse getHealthRecord(Long userId);

    /**
     * 检查用户是否已完善健康档案
     * 
     * @param userId 用户ID
     * @return true表示已完善，false表示未完善
     */
    boolean hasHealthRecord(Long userId);
}
