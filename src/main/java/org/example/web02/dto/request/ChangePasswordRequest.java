package org.example.web02.dto.request;

import lombok.Data;

/**
 * 修改密码请求 DTO
 *
 * 用于封装用户修改密码的请求参数，包含旧密码、新密码和确认密码
 */
@Data
public class ChangePasswordRequest {

    /** 旧密码 */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;

    /** 确认新密码 */
    private String confirmPassword;
}