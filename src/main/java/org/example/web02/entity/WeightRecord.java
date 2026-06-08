package org.example.web02.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class WeightRecord {

    private Long id;
    private Long userId;
    private BigDecimal weight;
    private Date recordDate;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private Integer isDeleted;
}