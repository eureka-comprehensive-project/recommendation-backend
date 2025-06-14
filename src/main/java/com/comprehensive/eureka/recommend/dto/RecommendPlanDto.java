package com.comprehensive.eureka.recommend.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendPlanDto {
    private PlanDto plan;
    private Double score;
    private String recommendationType;
    private List<BenefitDto> benefits;
}
