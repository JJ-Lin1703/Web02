package org.example.web02.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应封装类
 *
 * 用于封装所有 API 接口的返回结果，包含状态码、消息、数据和时间戳
 * 提供成功和失败的静态工厂方法，便于快速构建响应
 *
 * @param <T> 响应数据的类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** 响应状态码（200 表示成功，其他表示失败） */
    private Integer code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 响应时间戳（毫秒） */
    private Long timestamp;

    /**
     * 构建成功响应（带数据）
     *
     * @param data 响应数据
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构建成功响应（仅消息）
     *
     * @param message 成功消息
     * @return 成功响应对象
     */
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .code(200)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构建成功响应（带消息和数据）
     *
     * @param message 成功消息
     * @param data 响应数据
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构建失败响应
     *
     * @param code 错误状态码
     * @param message 错误消息
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}