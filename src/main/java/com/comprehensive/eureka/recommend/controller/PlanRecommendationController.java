package com.comprehensive.eureka.recommend.controller;

import com.comprehensive.eureka.recommend.dto.FeedbackDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.base.BaseResponseDto;
import com.comprehensive.eureka.recommend.dto.response.RecommendationResponseDto;
import com.comprehensive.eureka.recommend.service.PlanRecommendationService;
import com.comprehensive.eureka.recommend.service.UserPreferenceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/recommend")
public class PlanRecommendationController {

    private final UserPreferenceService userPreferenceService;
    private final PlanRecommendationService planRecommendationService;

    @PostMapping("/{userId}")
    public BaseResponseDto<RecommendationResponseDto> recommendPlan(@PathVariable Long userId, @RequestBody UserPreferenceDto userPreferenceDto) {
        userPreferenceService.updateUserPreference(userId, userPreferenceDto);
        RecommendationResponseDto recommendation = planRecommendationService.recommendPlan(userId, null, null);
        return BaseResponseDto.success(recommendation);
    }

    @GetMapping("/keyword/{keyword}")
    public BaseResponseDto<List<RecommendPlanDto>> recommendPlanByKeyword(@PathVariable String keyword) {
        List<RecommendPlanDto> recommendations = planRecommendationService.recommendPlanByKeyword(keyword, null);
        return BaseResponseDto.success(recommendations);
    }

    @PostMapping("/feedback/{userId}/{planId}")
    public BaseResponseDto<RecommendationResponseDto> submitFeedback(@PathVariable Long userId, @PathVariable Integer planId, @RequestBody FeedbackDto feedbackDto) {
        userPreferenceService.updateUserPreference(userId, feedbackDto);
        RecommendationResponseDto recommendation = null;

        log.info("keyword: {}, sentimentCode: {}, detailCode: {}",
                 feedbackDto.getKeyword(), feedbackDto.getSentimentCode(), feedbackDto.getDetailCode());

        if (feedbackDto.getKeyword() == null || feedbackDto.getKeyword().isEmpty())
            recommendation = planRecommendationService.recommendPlan(userId, feedbackDto, planId);

        else {
            recommendation = RecommendationResponseDto.builder()
                    .recommendPlans(planRecommendationService.recommendPlanByKeyword(feedbackDto.getKeyword(), feedbackDto))
                    .build();
        }

        return BaseResponseDto.success(recommendation);
    }
}
