package com.comprehensive.eureka.recommend.service;

import com.comprehensive.eureka.recommend.dto.RecommendationDto;
import java.util.List;

public interface PlanRecommendationService {

    List<RecommendationDto> recommendPlan(Long userId);
}
