package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.entity.WarningLog;
import org.example.web02.service.WarningLogService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预警日志控制器
 *
 * 提供预警日志相关的 API 接口，包括获取预警列表、未读预警、未读数量
 * 以及标记已读、删除预警等功能
 */
@RestController
@RequestMapping("/api/warnings")
public class WarningLogController {

    /** 预警日志服务，用于处理预警日志相关的业务逻辑 */
    private final WarningLogService warningLogService;

    /**
     * 构造函数，注入预警日志服务
     *
     * @param warningLogService 预警日志服务实例
     */
    public WarningLogController(WarningLogService warningLogService) {
        this.warningLogService = warningLogService;
    }

    /**
     * 获取当前用户的所有预警日志
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含预警日志列表的响应
     */
    @GetMapping
    public ApiResponse<List<WarningLog>> getWarnings(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务获取所有预警日志
        List<WarningLog> warnings = warningLogService.getWarnings(userId);
        return ApiResponse.success(warnings);
    }

    /**
     * 获取当前用户的未读预警日志
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含未读预警日志列表的响应
     */
    @GetMapping("/unread")
    public ApiResponse<List<WarningLog>> getUnreadWarnings(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务获取未读预警日志
        List<WarningLog> warnings = warningLogService.getUnreadWarnings(userId);
        return ApiResponse.success(warnings);
    }

    /**
     * 获取当前用户的未读预警数量
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含未读预警数量的响应
     */
    @GetMapping("/unread/count")
    public ApiResponse<Map<String, Long>> getUnreadCount(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务统计未读预警数量
        long count = warningLogService.countUnreadWarnings(userId);
        // 构建返回结果
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return ApiResponse.success(result);
    }

    /**
     * 标记当前用户的所有预警为已读
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 操作结果响应
     */
    @PostMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务标记所有预警为已读
        warningLogService.markAllAsRead(userId);
        return ApiResponse.success("全部已读");
    }

    /**
     * 标记指定预警为已读
     *
     * @param id 预警日志 ID
     * @return 操作结果响应
     */
    @PostMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        // 调用服务标记指定预警为已读
        warningLogService.markAsRead(id);
        return ApiResponse.success("已读");
    }

    /**
     * 删除指定预警日志
     *
     * @param id 预警日志 ID
     * @return 操作结果响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWarning(@PathVariable Long id) {
        // 调用服务删除预警日志
        warningLogService.deleteWarning(id);
        return ApiResponse.success("删除成功");
    }
}