package com.comprehensive.eureka.recommend.dto.response;

import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendationResponseDto {
    private UserPreferenceDto userPreference;
    private Double avgDataUsage;
    List<RecommendPlanDto> recommendPlans;
}
