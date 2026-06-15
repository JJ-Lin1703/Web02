package org.example.web02.dto.request;

import lombok.Data;

/**
 * 注册请求 DTO
 *
 * 用于封装用户注册的请求参数，包含用户名、密码和确认密码
 */
@Data
public class RegisterRequest {

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;

    /** 确认密码 */
    private String confirmPassword;
}