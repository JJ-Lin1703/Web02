package org.example.web02.service;

import org.example.web02.dto.request.ChangePasswordRequest;
import org.example.web02.dto.request.LoginRequest;
import org.example.web02.dto.request.RegisterRequest;
import org.example.web02.dto.response.LoginResponse;
import org.example.web02.dto.response.UserInfoResponse;
import org.example.web02.entity.User;

import java.util.List;

/**
 * 用户服务接口
 * 提供用户注册、登录、密码管理等核心认证功能
 */
public interface UserService {

    /**
     * 用户注册
     * 
     * @param request 注册请求DTO，包含用户名、密码、确认密码
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     * 
     * @param request 登录请求DTO，包含用户名和密码
     * @return 登录响应，包含JWT令牌和用户基本信息
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息响应DTO
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param request 密码修改请求DTO，包含旧密码、新密码、确认密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 根据ID查找用户
     * 
     * @param userId 用户ID
     * @return 用户实体
     */
    User findById(Long userId);

    /**
     * 获取所有用户列表（管理端）
     * 
     * @return 用户信息列表
     */
    List<UserInfoResponse> getAllUsers();

    /**
     * 重置用户密码（管理端）
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    void resetUserPassword(Long userId, String newPassword);
}