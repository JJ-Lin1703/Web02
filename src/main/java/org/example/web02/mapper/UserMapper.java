package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.User;

import java.util.Date;
import java.util.List;

/**
 * 用户数据访问接口
 *
 * 提供用户表的 CRUD 操作，包括查询、插入、更新、密码重置等功能
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户实体，不存在则返回 null
     */
    User findById(Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体，不存在则返回 null
     */
    User findByUsername(String username);

    /**
     * 插入新用户
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int update(User user);

    /**
     * 更新登录失败次数和锁定时间
     *
     * @param id 用户 ID
     * @param failCount 登录失败次数
     * @param lockUntil 锁定截止时间
     * @return 影响行数
     */
    int updateLoginFailCount(@Param("id") Long id, @Param("failCount") Integer failCount, @Param("lockUntil") Date lockUntil);

    /**
     * 更新用户密码
     *
     * @param id 用户 ID
     * @param password 新密码（加密后）
     * @return 影响行数
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 查询所有用户（管理员功能）
     *
     * @return 用户列表
     */
    List<User> findAllUsers();

    /**
     * 重置用户密码（管理员功能）
     *
     * @param id 用户 ID
     * @param password 新密码（加密后）
     * @return 影响行数
     */
    int resetPassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 查询所有活跃用户 ID（用于定时任务）
     *
     * @return 活跃用户 ID 列表
     */
    List<Long> findAllActiveUserIds();
}