package org.example.web02.controller;

import org.example.web02.dto.request.ChangePasswordRequest;
import org.example.web02.dto.request.LoginRequest;
import org.example.web02.dto.request.RegisterRequest;
import org.example.web02.dto.response.ApiResponse;
import org.example.web02.dto.response.LoginResponse;
import org.example.web02.dto.response.UserInfoResponse;
import org.example.web02.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * 提供用户认证相关的 API 接口，包括注册、登录、获取用户信息和修改密码等功能
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** 用户服务，用于处理用户认证和管理的业务逻辑 */
    private final UserService userService;

    /**
     * 构造函数，注入用户服务
     *
     * @param userService 用户服务实例
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     *
     * @param request 注册请求，包含用户名、密码等信息
     * @return 操作结果响应
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest request) {
        // 调用用户服务处理注册逻辑
        userService.register(request);
        return ApiResponse.success("注册成功");
    }

    /**
     * 用户登录
     *
     * @param request 登录请求，包含用户名和密码
     * @return 包含 JWT 令牌和用户信息的响应
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        // 调用用户服务处理登录逻辑，返回 JWT 令牌
        LoginResponse response = userService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含用户信息的响应
     */
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getCurrentUser(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用用户服务获取用户信息
        UserInfoResponse response = userService.getUserInfo(userId);
        return ApiResponse.success(response);
    }

    /**
     * 修改用户密码
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param request 修改密码请求，包含旧密码和新密码
     * @return 操作结果响应
     */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(Authentication authentication, 
                                            @RequestBody ChangePasswordRequest request) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用用户服务修改密码
        userService.changePassword(userId, request);
        return ApiResponse.success("密码修改成功");
    }
}