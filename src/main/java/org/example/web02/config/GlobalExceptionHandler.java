package org.example.web02.config;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * <p>
 * 使用 @RestControllerAdvice 注解，统一处理控制器层抛出的各种异常。
 * 将异常转换为统一的 ApiResponse 格式返回给前端，提升用户体验和系统可维护性。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常对象
     * @return 包含错误码和错误信息的响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getCode())
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    /**
     * 处理参数校验异常
     * <p>
     * 当使用 @Valid 注解进行参数校验失败时触发。
     * 收集所有字段的校验错误信息，返回详细的错误字段和错误提示。
     *
     * @param ex 方法参数校验异常
     * @return 包含字段名和错误信息的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        // 遍历所有校验错误，提取字段名和错误信息
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest()
                .body(ApiResponse.<Map<String, String>>builder()
                        .code(400)
                        .message("参数校验失败")
                        .data(errors)
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

    /**
     * 处理文件上传异常
     *
     * @param e 文件上传异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(MultipartException e) {
        log.error("文件上传异常", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "文件上传失败：" + e.getMessage()));
    }

    /**
     * 处理文件大小超出限制异常
     *
     * @param e 文件大小超出限制异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "文件大小超出限制"));
    }

    /**
     * 处理请求体解析失败异常
     * <p>
     * 当请求体格式不正确（如 JSON 格式错误）时触发。
     *
     * @param e HTTP 消息不可读异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("请求体解析失败", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "请求数据格式错误：" + (e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage())));
    }

    /**
     * 处理通用异常
     * <p>
     * 兜底异常处理器，捕获所有未被上述处理器处理的异常。
     * 记录错误日志并返回服务器内部错误信息。
     *
     * @param e 通用异常对象
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("未处理的异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误：" + e.getMessage()));
    }
}