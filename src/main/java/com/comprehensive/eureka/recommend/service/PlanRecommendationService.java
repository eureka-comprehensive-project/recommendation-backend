package com.comprehensive.eureka.recommend.service;

import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.response.RecommendationResponseDto;
import java.util.List;

public interface PlanRecommendationService {

    RecommendationResponseDto recommendPlan(Long userId);
    List<RecommendPlanDto> recommendPlanByKeyword(String keyword);
}
