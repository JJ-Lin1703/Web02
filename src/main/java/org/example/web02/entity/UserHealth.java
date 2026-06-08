package org.example.web02.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserHealth {

    private Long id;
    private Long userId;
    private Integer age;
    private Integer gender;
    private BigDecimal height;
    private BigDecimal weight;
    private Integer activityLevel;
    private String dietHobby;
    private String healthTarget;
    private String allergy;
    private String medicalHistory;
    private BigDecimal bmr;
    private BigDecimal tdee;
    private BigDecimal bmi;
    private Date updateTime;
    private Date createTime;
    private Integer isDeleted;
}
