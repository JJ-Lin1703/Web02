package org.example.web02.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户信息响应 DTO
 *
 * 用于封装用户信息的返回结果，包含用户 ID、用户名、角色和创建时间
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    /** 用户 ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 用户角色（0-普通用户，1-管理员） */
    private Integer role;

    /** 用户创建时间 */
    private Date createTime;
}