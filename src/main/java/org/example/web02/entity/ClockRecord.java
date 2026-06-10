package org.example.web02.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ClockRecord {

    private Long id;
    private Long userId;
    private Long planId;
    private Date recordDate;
    private String finishItem;
    private String totalItem;
    private String unfinishReason;
    private BigDecimal finishRate;
    private Date createTime;
    private Date updateTime;
    private Integer isDeleted;
}