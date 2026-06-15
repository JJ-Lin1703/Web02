package org.example.web02.entity;

import lombok.Data;

import java.util.Date;

/**
 * 预警日志实体类
 *
 * 对应数据库表 warning_log，存储用户的预警通知记录，
 * 包括预警类型、预警内容、是否已读等
 */
@Data
public class WarningLog {

    /** 预警日志 ID（主键） */
    private Long id;

    /** 用户 ID（外键，关联 user 表） */
    private Long userId;

    /** 预警类型 */
    private String warningType;

    /** 预警内容 */
    private String warningContent;

    /** 是否已读（0-未读，1-已读） */
    private Integer isRead;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 是否删除（0-未删除，1-已删除，软删除标记） */
    private Integer isDeleted;

    /** 预警类型常量：体重波动异常 */
    public static final String TYPE_WEIGHT_FLUCTUATION = "weight_fluctuation";

    /** 预警类型常量：打卡遗漏 */
    public static final String TYPE_CLOCK_MISS = "clock_miss";

    /** 预警类型常量：BMI 异常 */
    public static final String TYPE_BMI_ABNORMAL = "bmi_abnormal";

    /** 预警类型常量：签到提醒 */
    public static final String TYPE_CHECKIN_REMIND = "checkin_remind";

    /** 预警类型常量：体重记录提醒 */
    public static final String TYPE_WEIGHT_RECORD_REMIND = "weight_record_remind";

    /**
     * 判断预警是否未读
     *
     * @return true-未读，false-已读
     */
    public boolean isUnread() {
        return isRead == null || isRead == 0;
    }
}