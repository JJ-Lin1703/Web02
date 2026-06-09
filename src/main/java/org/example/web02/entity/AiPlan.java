package org.example.web02.entity;

import lombok.Data;

import java.util.Date;

@Data
public class AiPlan {

    private Long id;
    private Long userId;
    private String planTitle;
    private Integer totalCalorie;
    private String planContent;
    private Integer versionNo;
    private Long parentPlanId;
    private Date generateTime;
    private Date updateTime;
    private Integer isDeleted;
}
