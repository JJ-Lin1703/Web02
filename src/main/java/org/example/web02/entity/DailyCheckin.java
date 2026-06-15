package org.example.web02.entity;

import lombok.Data;

import java.util.Date;

/**
 * 每日签到实体类
 *
 * 对应数据库表 daily_checkin，存储用户的每日签到记录，
 * 包括签到日期、签到时间、连续签到天数等
 */
@Data
public class DailyCheckin {

    /** 签到记录 ID（主键） */
    private Long id;

    /** 用户 ID（外键，关联 user 表） */
    private Long userId;

    /** 签到日期 */
    private Date checkinDate;

    /** 签到时间 */
    private Date checkinTime;

    /** 连续签到天数 */
    private Integer continuousDays;

    /** 创建时间 */
    private Date createTime;
}
