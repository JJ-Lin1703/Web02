package org.example.web02.config;

import lombok.extern.slf4j.Slf4j;
import org.example.web02.entity.User;
import org.example.web02.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initializeAdmin();
    }

    private void initializeAdmin() {
        User existingAdmin = userMapper.findByUsername(adminUsername);
        if (existingAdmin == null) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(1);
            admin.setLoginFailCount(0);
            admin.setIsDeleted(0);
            admin.setCreateTime(new Date());
            admin.setUpdateTime(new Date());

            userMapper.insert(admin);
            log.info("默认管理员账号创建成功！用户名: {}, 密码: {}", adminUsername, adminPassword);
            log.warn("请在首次登录后立即修改默认密码！");
        } else if (!existingAdmin.isAdmin()) {
            existingAdmin.setRole(1);
            existingAdmin.setUpdateTime(new Date());
            userMapper.update(existingAdmin);
            log.info("已将用户 {} 升级为管理员", adminUsername);
        } else {
            log.info("管理员账号已存在，无需创建");
        }
    }
}
