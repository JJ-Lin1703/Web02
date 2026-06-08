package org.example.web02.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class HealthRecordResponse {

    private Long id;
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
}
