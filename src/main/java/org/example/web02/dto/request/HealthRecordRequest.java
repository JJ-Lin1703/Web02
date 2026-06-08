package org.example.web02.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HealthRecordRequest {

    private Integer age;
    private Integer gender;
    private BigDecimal height;
    private BigDecimal weight;
    private Integer activityLevel;
    private String dietHobby;
    private String healthTarget;
    private String allergy;
    private String medicalHistory;
}
