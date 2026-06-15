package org.example.web02.controller;

import org.example.web02.dto.request.HealthRecordRequest;
import org.example.web02.dto.response.ApiResponse;
import org.example.web02.dto.response.HealthRecordResponse;
import org.example.web02.service.UserHealthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 健康档案控制器
 *
 * 提供健康档案相关的 API 接口，包括获取、创建、更新健康档案
 * 以及检查健康档案是否存在等功能
 */
@RestController
@RequestMapping("/api/health-record")
public class HealthRecordController {

    /** 用户健康服务，用于处理健康档案相关的业务逻辑 */
    private final UserHealthService userHealthService;

    /**
     * 构造函数，注入用户健康服务
     *
     * @param userHealthService 用户健康服务实例
     */
    public HealthRecordController(UserHealthService userHealthService) {
        this.userHealthService = userHealthService;
    }

    /**
     * 获取当前用户的健康档案
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含健康档案信息的响应
     */
    @GetMapping
    public ApiResponse<HealthRecordResponse> getHealthRecord(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务获取健康档案
        HealthRecordResponse response = userHealthService.getHealthRecord(userId);
        return ApiResponse.success(response);
    }

    /**
     * 创建健康档案
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param request 健康档案请求，包含身高、体重、年龄等信息
     * @return 包含创建后的健康档案的响应
     */
    @PostMapping
    public ApiResponse<HealthRecordResponse> createHealthRecord(Authentication authentication,
                                                               @RequestBody HealthRecordRequest request) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务创建健康档案
        HealthRecordResponse response = userHealthService.createHealthRecord(userId, request);
        return ApiResponse.success("健康档案创建成功", response);
    }

    /**
     * 更新健康档案
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param request 健康档案请求，包含更新的身高、体重、年龄等信息
     * @return 包含更新后的健康档案的响应
     */
    @PutMapping
    public ApiResponse<HealthRecordResponse> updateHealthRecord(Authentication authentication,
                                                               @RequestBody HealthRecordRequest request) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务更新健康档案
        HealthRecordResponse response = userHealthService.updateHealthRecord(userId, request);
        return ApiResponse.success("健康档案更新成功", response);
    }

    /**
     * 检查健康档案是否存在
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含健康档案存在状态的响应
     */
    @GetMapping("/exists")
    public ApiResponse<HealthRecordExistsResponse> checkHealthRecordExists(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务检查健康档案是否存在
        boolean exists = userHealthService.hasHealthRecord(userId);
        return ApiResponse.success(new HealthRecordExistsResponse(exists));
    }

    /**
     * 健康档案存在状态响应内部类
     *
     * 用于封装健康档案是否存在的结果
     */
    private static class HealthRecordExistsResponse {

        /** 健康档案是否存在 */
        private final boolean exists;

        /**
         * 构造函数
         *
         * @param exists 健康档案是否存在
         */
        public HealthRecordExistsResponse(boolean exists) {
            this.exists = exists;
        }

        /**
         * 获取健康档案是否存在
         *
         * @return 是否存在
         */
        public boolean isExists() {
            return exists;
        }
    }
}
