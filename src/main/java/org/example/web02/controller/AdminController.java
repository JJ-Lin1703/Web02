package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.dto.response.UserInfoResponse;
import org.example.web02.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ApiResponse<List<UserInfoResponse>> getAllUsers() {
        List<UserInfoResponse> users = userService.getAllUsers();
        return ApiResponse.success(users);
    }

    @PostMapping("/users/{userId}/reset-password")
    public ApiResponse<Void> resetUserPassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        userService.resetUserPassword(userId, newPassword);
        return ApiResponse.success("密码重置成功");
    }
}