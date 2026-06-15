package org.example.web02.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证入口点
 *
 * 当用户未认证访问受保护资源时，Spring Security 会调用此类处理异常
 * 返回 401 未授权的 JSON 响应
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /** JSON 对象映射器，用于将响应数据转换为 JSON 格式 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理认证异常
     *
     * @param request HTTP 请求对象
     * @param response HTTP 响应对象
     * @param authException 认证异常信息
     * @throws IOException 写入响应流时可能抛出的 IO 异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 设置响应内容类型为 JSON，字符编码为 UTF-8
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        // 设置 HTTP 状态码为 401 未授权
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 构建响应体
        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", "未授权，请先登录");
        body.put("timestamp", System.currentTimeMillis());

        // 将响应体写入输出流
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}