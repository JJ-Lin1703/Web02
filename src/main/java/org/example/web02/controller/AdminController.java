package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.dto.response.UserInfoResponse;
import org.example.web02.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 *
 * 提供管理员相关的 API 接口，包括用户管理和密码重置等功能
 * 所有接口需要 ADMIN 角色权限才能访问
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /** 用户服务，用于处理用户相关业务逻辑 */
    private final UserService userService;

    /**
     * 构造函数，注入用户服务
     *
     * @param userService 用户服务实例
     */
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取所有用户列表
     *
     * @return 包含所有用户信息的响应
     */
    @GetMapping("/users")
    public ApiResponse<List<UserInfoResponse>> getAllUsers() {
        // 调用用户服务获取所有用户信息
        List<UserInfoResponse> users = userService.getAllUsers();
        return ApiResponse.success(users);
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户 ID
     * @param request 包含新密码的请求体
     * @return 操作结果响应
     */
    @PostMapping("/users/{userId}/reset-password")
    public ApiResponse<Void> resetUserPassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        // 从请求体中获取新密码
        String newPassword = request.get("newPassword");
        // 调用用户服务重置密码
        userService.resetUserPassword(userId, newPassword);
        return ApiResponse.success("密码重置成功");
    }
}