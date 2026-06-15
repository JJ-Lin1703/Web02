package org.example.web02.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * 用于封装业务逻辑中的异常情况，包含错误码和错误消息。
 * 继承自 RuntimeException，为非受检异常，无需显式捕获。
 * 通常由 GlobalExceptionHandler 统一处理并返回给前端。
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误状态码（如 400、401、403、404、500 等） */
    private final Integer code;

    /**
     * 构造函数，使用默认错误码 400
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 构造函数，指定错误码和错误消息
     *
     * @param code 错误状态码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}