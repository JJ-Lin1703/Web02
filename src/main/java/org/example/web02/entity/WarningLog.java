package org.example.web02.entity;

import lombok.Data;

import java.util.Date;

@Data
public class WarningLog {

    private Long id;
    private Long userId;
    private String warningType;
    private String warningContent;
    private Integer isRead;
    private Date createTime;
    private Date updateTime;
    private Integer isDeleted;

    public static final String TYPE_WEIGHT_FLUCTUATION = "weight_fluctuation";
    public static final String TYPE_CLOCK_MISS = "clock_miss";
    public static final String TYPE_BMI_ABNORMAL = "bmi_abnormal";
    public static final String TYPE_CHECKIN_REMIND = "checkin_remind";
    public static final String TYPE_WEIGHT_RECORD_REMIND = "weight_record_remind";

    public boolean isUnread() {
        return isRead == null || isRead == 0;
    }
}