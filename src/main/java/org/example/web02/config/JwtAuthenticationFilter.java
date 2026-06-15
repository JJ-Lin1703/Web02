package org.example.web02.config;

import org.example.web02.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 *
 * 继承 OncePerRequestFilter，确保每个请求只执行一次过滤
 * 从请求头中提取 JWT 令牌，验证后将用户认证信息存入 SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT 工具类，用于令牌解析和验证 */
    private final JwtUtil jwtUtil;

    /**
     * 构造函数，注入 JWT 工具类
     *
     * @param jwtUtil JWT 工具类实例
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 执行过滤逻辑
     *
     * @param request HTTP 请求对象
     * @param response HTTP 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求中提取 JWT 令牌
            String jwt = extractJwtFromRequest(request);

            // 如果令牌存在且有效，则设置用户认证信息
            if (StringUtils.hasText(jwt) && jwtUtil.isTokenValid(jwt)) {
                // 从令牌中提取用户 ID 和角色
                Long userId = Long.parseLong(jwtUtil.extractUserId(jwt));
                Integer role = jwtUtil.extractRole(jwt);

                // 根据角色编号转换为 Spring Security 的角色名称
                String roleName = role == 1 ? "ROLE_ADMIN" : "ROLE_USER";

                // 创建认证令牌，包含用户 ID、权限信息
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority(roleName))
                        );
                // 设置认证详情（如 IP 地址、Session ID 等）
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 将认证信息存入 SecurityContext，供后续过滤器使用
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // 认证过程中出现异常，记录日志但不中断请求
            logger.error("Could not set user authentication in security context", ex);
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 JWT 令牌
     *
     * @param request HTTP 请求对象
     * @return JWT 令牌字符串，如果不存在则返回 null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // 获取 Authorization 请求头
        String bearerToken = request.getHeader("Authorization");
        // 检查请求头是否存在且以 "Bearer " 开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 去掉 "Bearer " 前缀，返回实际的 JWT 令牌
            return bearerToken.substring(7);
        }
        return null;
    }
}