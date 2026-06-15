package org.example.web02.entity;

import lombok.Data;

import java.util.Date;

/**
 * 用户实体类
 *
 * 对应数据库表 user，存储用户的基本信息，包括用户名、密码、角色、
 * 锁定状态、登录失败次数、运动天数等
 */
@Data
public class User {

    /** 用户 ID（主键） */
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（加密存储） */
    private String password;

    /** 用户角色（0-普通用户，1-管理员） */
    private Integer role;

    /** 锁定截止时间（账户被锁定时的解锁时间） */
    private Date lockUntil;

    /** 登录失败次数（连续登录失败累计次数） */
    private Integer loginFailCount;

    /** 运动天数（累计运动打卡天数） */
    private Integer exerciseDays;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 是否删除（0-未删除，1-已删除，软删除标记） */
    private Integer isDeleted;

    /**
     * 判断用户是否为管理员
     *
     * @return true-管理员，false-普通用户
     */
    public boolean isAdmin() {
        return role != null && role == 1;
    }

    /**
     * 判断用户账户是否被锁定
     *
     * @return true-已锁定，false-未锁定
     */
    public boolean isLocked() {
        return lockUntil != null && lockUntil.after(new Date());
    }
}