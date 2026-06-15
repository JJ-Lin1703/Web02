package org.example.web02.config;

import lombok.extern.slf4j.Slf4j;
import org.example.web02.entity.User;
import org.example.web02.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 数据初始化器
 * <p>
 * 实现 CommandLineRunner 接口，在应用启动时自动执行初始化逻辑。
 * 主要负责创建默认管理员账号，确保系统启动后有可登录的管理员账户。
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    /** 用户数据访问层 */
    private final UserMapper userMapper;

    /** 密码编码器，用于加密用户密码 */
    private final PasswordEncoder passwordEncoder;

    /** 默认管理员用户名，从配置文件读取 */
    @Value("${app.admin.username}")
    private String adminUsername;

    /** 默认管理员密码，从配置文件读取 */
    @Value("${app.admin.password}")
    private String adminPassword;

    /**
     * 构造函数，依赖注入
     *
     * @param userMapper      用户数据访问层
     * @param passwordEncoder 密码编码器
     */
    public DataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 应用启动时执行的初始化方法
     *
     * @param args 命令行参数
     */
    @Override
    public void run(String... args) {
        initializeAdmin();
    }

    /**
     * 初始化默认管理员账号
     * <p>
     * 检查数据库中是否已存在管理员账号：
     * - 如果不存在：创建新的管理员账号
     * - 如果存在但不是管理员：将其升级为管理员
     * - 如果已存在管理员：跳过创建
     */
    private void initializeAdmin() {
        // 查询是否已存在同名用户
        User existingAdmin = userMapper.findByUsername(adminUsername);

        if (existingAdmin == null) {
            // 不存在则创建新管理员
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));  // 密码加密存储
            admin.setRole(1);              // 设置为管理员角色
            admin.setLoginFailCount(0);    // 登录失败次数初始化为0
            admin.setIsDeleted(0);         // 未删除状态
            admin.setCreateTime(new Date());
            admin.setUpdateTime(new Date());

            userMapper.insert(admin);
            log.info("默认管理员账号创建成功！用户名: {}, 密码: {}", adminUsername, adminPassword);
            log.warn("请在首次登录后立即修改默认密码！");
        } else if (!existingAdmin.isAdmin()) {
            // 存在同名用户但不是管理员，升级为管理员
            existingAdmin.setRole(1);
            existingAdmin.setUpdateTime(new Date());
            userMapper.update(existingAdmin);
            log.info("已将用户 {} 升级为管理员", adminUsername);
        } else {
            // 管理员账号已存在
            log.info("管理员账号已存在，无需创建");
        }
    }
}
