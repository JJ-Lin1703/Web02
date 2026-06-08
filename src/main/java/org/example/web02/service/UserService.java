package org.example.web02.service;

import org.example.web02.dto.request.ChangePasswordRequest;
import org.example.web02.dto.request.LoginRequest;
import org.example.web02.dto.request.RegisterRequest;
import org.example.web02.dto.response.LoginResponse;
import org.example.web02.dto.response.UserInfoResponse;
import org.example.web02.entity.User;

import java.util.List;

public interface UserService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserInfoResponse getUserInfo(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);

    User findById(Long userId);

    List<UserInfoResponse> getAllUsers();

    void resetUserPassword(Long userId, String newPassword);
}