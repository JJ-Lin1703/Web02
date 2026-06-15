package org.example.web02.dto.request;

import lombok.Data;

/**
 * 登录请求 DTO
 *
 * 用于封装用户登录的请求参数，包含用户名和密码
 */
@Data
public class LoginRequest {

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;
}