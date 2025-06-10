package com.comprehensive.eureka.recommend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendationDto {
    private PlanDto plan;
    private Double score;
    private String recommendationType;
}
