package org.example.web02.dto.request;

import lombok.Data;

@Data
public class TweakPlanRequest {

    private Long planId;
    private String dayName;
    private String module;
    private String itemDesc;
    private String reason;
}