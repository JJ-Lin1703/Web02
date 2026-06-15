package org.example.web02.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 *
 * 配置视图控制器，实现单页应用（SPA）的路由支持
 * 所有非 API 请求都转发到 index.html，由前端路由处理
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 添加视图控制器
     *
     * 将所有非 /api 开头的请求转发到 index.html
     * 这样前端路由（如 /home、/history）可以正常工作
     *
     * @param registry 视图控制器注册表
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 匹配所有不以 "api" 开头的路径（使用正则表达式排除 API 请求）
        registry.addViewController("/{path:^(?!api).*$}")
                // 转发到 index.html，由前端路由处理
                .setViewName("forward:/index.html");
    }
}
