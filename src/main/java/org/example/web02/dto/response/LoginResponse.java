package org.example.web02.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 *
 * 用于封装用户登录成功后的返回信息，包含 JWT 令牌、用户 ID、用户名、角色和过期时间
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT 令牌 */
    private String token;

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户角色（0-普通用户，1-管理员） */
    private Integer role;

    /** 令牌过期时间（毫秒时间戳） */
    private Long expiresAt;
}