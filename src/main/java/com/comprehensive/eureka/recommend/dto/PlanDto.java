package com.comprehensive.eureka.recommend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanDto {
    private Integer planId;
    private String planName;
    private Integer price;
    private Integer dataAllowance;
    private String dataAllowanceUnit;
    private Integer sharedDataAllowance;
    private String sharedDataAllowanceUnit;
    private Integer voiceCallAmount;
    private Integer valueAddedCallAmount;
    private boolean isFamilyDataEnabled;
    private String category;
}
