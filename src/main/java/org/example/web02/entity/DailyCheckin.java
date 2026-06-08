package org.example.web02.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DailyCheckin {

    private Long id;
    private Long userId;
    private Date checkinDate;
    private Date checkinTime;
    private Integer continuousDays;
    private Date createTime;
}
