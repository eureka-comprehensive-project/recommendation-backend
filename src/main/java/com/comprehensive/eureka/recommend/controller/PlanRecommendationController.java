package com.comprehensive.eureka.recommend.controller;

import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.base.BaseResponseDto;
import com.comprehensive.eureka.recommend.dto.response.RecommendationResponseDto;
import com.comprehensive.eureka.recommend.service.PlanRecommendationService;
import com.comprehensive.eureka.recommend.service.UserPreferenceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlanRecommendationController {

    private final UserPreferenceService userPreferenceService;
    private final PlanRecommendationService planRecommendationService;

    @PostMapping("/recommend/{userId}")
    public BaseResponseDto<RecommendationResponseDto> recommendPlan(@PathVariable Long userId, @RequestBody UserPreferenceDto userPreferenceDto) {
        userPreferenceService.updateUserPreference(userId, userPreferenceDto);
        RecommendationResponseDto recommendation = planRecommendationService.recommendPlan(userId);
        return BaseResponseDto.success(recommendation);
    }
}
