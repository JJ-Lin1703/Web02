package org.example.web02.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 体重记录实体类
 *
 * 对应数据库表 weight_record，存储用户的体重记录信息，
 * 包括体重值、记录日期、备注等
 */
@Data
public class WeightRecord {

    /** 记录 ID（主键） */
    private Long id;

    /** 用户 ID（外键，关联 user 表） */
    private Long userId;

    /** 体重值（千克） */
    private BigDecimal weight;

    /** 记录日期 */
    private Date recordDate;

    /** 备注（可选） */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 是否删除（0-未删除，1-已删除，软删除标记） */
    private Integer isDeleted;
}
