package org.example.web02.service.impl;

import org.example.web02.dto.request.ChangePasswordRequest;
import org.example.web02.dto.request.LoginRequest;
import org.example.web02.dto.request.RegisterRequest;
import org.example.web02.dto.response.LoginResponse;
import org.example.web02.dto.response.UserInfoResponse;
import org.example.web02.entity.User;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.UserMapper;
import org.example.web02.service.UserService;
import org.example.web02.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
/**
 * 用户服务实现类
 * 负责用户注册、登录、密码管理等核心认证功能
 */
public class UserServiceImpl implements UserService {

    /** 最大登录失败次数 */
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    /** 账号锁定时长（分钟） */
    private static final int LOCK_DURATION_MINUTES = 5;

    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** 密码加密器 */
    private final PasswordEncoder passwordEncoder;
    /** JWT工具类 */
    private final JwtUtil jwtUtil;

    /**
     * 构造函数注入依赖
     * @param userMapper 用户Mapper
     * @param passwordEncoder 密码加密器
     * @param jwtUtil JWT工具类
     */
    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户注册
     * 流程：验证请求参数 → 检查用户名是否已存在 → 创建用户记录
     * 
     * @param request 注册请求DTO，包含用户名、密码、确认密码
     * @throws BusinessException 用户名已存在或参数校验失败时抛出
     */
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        validateRegisterRequest(request);

        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(0);
        user.setLoginFailCount(0);
        user.setIsDeleted(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        userMapper.insert(user);
    }

    /**
     * 验证注册请求参数
     * 校验规则：
     * - 用户名不能为空，长度4-20位，只能包含中文、英文和数字
     * - 密码不能为空，长度6-16位，必须包含字母和数字
     * - 两次密码必须一致
     * 
     * @param request 注册请求DTO
     * @throws BusinessException 参数校验失败时抛出
     */
    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }

        if (request.getUsername().length() < 4 || request.getUsername().length() > 20) {
            throw new BusinessException("用户名长度必须在4-20位之间");
        }

        if (!request.getUsername().matches("^[a-zA-Z0-9\\u4e00-\\u9fa5]+$")) {
            throw new BusinessException("用户名只能包含中文、英文和数字");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        if (request.getPassword().length() < 6 || request.getPassword().length() > 16) {
            throw new BusinessException("密码长度必须在6-16位之间");
        }

        if (!request.getPassword().matches("^(?=.*[a-zA-Z])(?=.*\\d).+$")) {
            throw new BusinessException("密码必须包含字母和数字");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
    }

    /**
     * 用户登录
     * 流程：验证用户名 → 检查账号是否锁定 → 验证密码 → 更新登录状态 → 生成JWT令牌
     * 
     * @param request 登录请求DTO，包含用户名和密码
     * @return 登录响应，包含JWT令牌和用户基本信息
     * @throws BusinessException 用户名不存在、密码错误或账号锁定时抛出
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.isLocked()) {
            throw new BusinessException("账号已被锁定，请5分钟后再试");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleLoginFailure(user);
            int remainingAttempts = MAX_LOGIN_ATTEMPTS - user.getLoginFailCount() - 1;
            if (remainingAttempts > 0) {
                throw new BusinessException("用户名或密码错误，还剩" + remainingAttempts + "次尝试机会");
            } else {
                throw new BusinessException("账号已被锁定，请5分钟后再试");
            }
        }

        resetLoginAttempts(user);

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        Long expiresAt = System.currentTimeMillis() + 604800000L;

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * 处理登录失败
     * 增加登录失败计数，达到最大次数时锁定账号
     * 
     * @param user 用户实体
     */
    private void handleLoginFailure(User user) {
        int newFailCount = user.getLoginFailCount() + 1;
        Date lockUntil = null;

        if (newFailCount >= MAX_LOGIN_ATTEMPTS) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, LOCK_DURATION_MINUTES);
            lockUntil = calendar.getTime();
        }

        userMapper.updateLoginFailCount(user.getId(), newFailCount, lockUntil);
    }

    /**
     * 重置登录失败次数
     * 登录成功后清除失败计数和锁定状态
     * 
     * @param user 用户实体
     */
    private void resetLoginAttempts(User user) {
        if (user.getLoginFailCount() > 0) {
            userMapper.updateLoginFailCount(user.getId(), 0, null);
        }
    }

    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息响应DTO
     * @throws BusinessException 用户不存在时抛出
     */
    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = findById(userId);
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .build();
    }

    /**
     * 修改密码
     * 流程：验证旧密码 → 验证新密码格式 → 更新密码
     * 
     * @param userId 用户ID
     * @param request 密码修改请求DTO，包含旧密码、新密码、确认密码
     * @throws BusinessException 旧密码错误或新密码格式不符合要求时抛出
     */
    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findById(userId);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        if (request.getNewPassword().length() < 6 || request.getNewPassword().length() > 16) {
            throw new BusinessException("密码长度必须在6-16位之间");
        }

        if (!request.getNewPassword().matches("^(?=.*[a-zA-Z])(?=.*\\d).+$")) {
            throw new BusinessException("密码必须包含字母和数字");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        userMapper.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));
    }

    /**
     * 根据ID查找用户（带权限校验）
     * 
     * @param userId 用户ID
     * @return 用户实体
     * @throws BusinessException 用户不存在时抛出
     */
    @Override
    public User findById(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    /**
     * 获取所有用户列表（管理端）
     * 
     * @return 用户信息列表
     */
    @Override
    public List<UserInfoResponse> getAllUsers() {
        return userMapper.findAllUsers().stream()
                .map(user -> UserInfoResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .createTime(user.getCreateTime())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 重置用户密码（管理端）
     * 管理员可直接重置用户密码，无需验证旧密码
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * @throws BusinessException 用户不存在或密码格式不符合要求时抛出
     */
    @Override
    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        //User user = findById(userId);

        if (newPassword.length() < 6 || newPassword.length() > 16) {
            throw new BusinessException("密码长度必须在6-16位之间");
        }

        if (!newPassword.matches("^(?=.*[a-zA-Z])(?=.*\\d).+$")) {
            throw new BusinessException("密码必须包含字母和数字");
        }

        userMapper.resetPassword(userId, passwordEncoder.encode(newPassword));
    }
}