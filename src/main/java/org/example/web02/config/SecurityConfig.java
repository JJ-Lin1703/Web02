package org.example.web02.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 配置类
 *
 * 配置 JWT 认证、权限控制、跨域访问等安全策略
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /** JWT 认证入口点，处理未授权访问 */
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /** JWT 认证过滤器，从请求中提取并验证 JWT 令牌 */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造函数，注入 JWT 相关组件
     *
     * @param jwtAuthenticationEntryPoint JWT 认证入口点
     * @param jwtAuthenticationFilter JWT 认证过滤器
     */
    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置安全过滤器链
     *
     * @param http HTTP 安全配置对象
     * @return 安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 启用跨域支持
            .cors()
            .and()
            // 禁用 CSRF（JWT 不需要 CSRF 保护）
            .csrf().disable()
            // 配置异常处理
            .exceptionHandling()
            // 设置自定义的认证入口点
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            // 配置会话管理
            .sessionManagement()
            // 使用无状态会话策略（JWT 认证不需要会话）
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 配置请求授权
            .authorizeRequests()
            // 允许注册和登录接口无需认证
            .antMatchers("/api/auth/register", "/api/auth/login").permitAll()
            // 管理员接口需要 ADMIN 角色
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            // 其他所有请求都需要认证
            .anyRequest().authenticated();

        // 在用户名密码认证过滤器之前添加 JWT 认证过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器 Bean
     *
     * 使用 BCrypt 算法对密码进行加密
     *
     * @return 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器 Bean
     *
     * 用于处理用户认证逻辑
     *
     * @param authConfig 认证配置对象
     * @return 认证管理器实例
     * @throws Exception 获取认证管理器时可能抛出的异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 跨域配置源 Bean
     *
     * 配置允许的跨域请求来源、方法和头部
     *
     * @return 跨域配置源实例
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源
        config.addAllowedOriginPattern("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有 HTTP 方法
        config.addAllowedMethod("*");
        // 允许携带凭证（如 Cookie）
        config.setAllowCredentials(true);

        // 注册跨域配置到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}