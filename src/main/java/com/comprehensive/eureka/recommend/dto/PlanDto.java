package com.comprehensive.eureka.recommend.dto;

import com.comprehensive.eureka.recommend.entity.enums.DataPeriod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanDto {
    private Integer planId;
    private String planName;
    private Integer monthlyFee;
    private Integer dataAllowance;
    private String dataAllowanceUnit;
    private DataPeriod dataPeriod;
    private Integer tetheringDataAmount;
    private String tetheringDataUnit;
    private Integer voiceCallAmount;
    private Integer additionalCallAllowance;
    private boolean isFamilyDataEnabled;
    private String planCategory;
}
