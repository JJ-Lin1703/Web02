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
public class UserServiceImpl implements UserService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 5;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

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

    private void resetLoginAttempts(User user) {
        if (user.getLoginFailCount() > 0) {
            userMapper.updateLoginFailCount(user.getId(), 0, null);
        }
    }

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

    @Override
    public User findById(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

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

    @Override
    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        User user = findById(userId);

        if (newPassword.length() < 6 || newPassword.length() > 16) {
            throw new BusinessException("密码长度必须在6-16位之间");
        }

        if (!newPassword.matches("^(?=.*[a-zA-Z])(?=.*\\d).+$")) {
            throw new BusinessException("密码必须包含字母和数字");
        }

        userMapper.resetPassword(userId, passwordEncoder.encode(newPassword));
    }
}