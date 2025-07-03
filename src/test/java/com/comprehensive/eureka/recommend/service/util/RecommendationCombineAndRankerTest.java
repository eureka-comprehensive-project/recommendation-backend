package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.dto.BenefitDto;
import com.comprehensive.eureka.recommend.dto.FeedbackDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.RecommendationResponseDto;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationCombineAndRankerTest {

    @Mock
    private PlanApiServiceClient planApiServiceClient;

    @InjectMocks
    private RecommendationCombineAndRanker recommendationCombineAndRanker;

    @Test
    @DisplayName("추천 결과 조합 및 랭킹 테스트 - 피드백 없음")
    void 추천_결과_조합_및_랭킹_테스트_피드백_없음() {
        // given
        List<RecommendPlanDto> userSimilarityResults = Arrays.asList(
                createRecommendPlanDto(1, 0.8, "USER_SIMILARITY"),
                createRecommendPlanDto(2, 0.7, "USER_SIMILARITY")
        );
        
        List<RecommendPlanDto> weightedResults = Arrays.asList(
                createRecommendPlanDto(2, 0.9, "WEIGHTED"),
                createRecommendPlanDto(3, 0.85, "WEIGHTED")
        );
        
        List<RecommendPlanDto> directSimilarityResults = Arrays.asList(
                createRecommendPlanDto(1, 0.75, "DIRECT_SIMILARITY"),
                createRecommendPlanDto(3, 0.7, "DIRECT_SIMILARITY")
        );
        
        UserPreferenceDto userPreference = UserPreferenceDto.builder()
                .preferenceDataUsage(10)
                .preferenceDataUsageUnit("GB")
                .preferencePrice(50000)
                .build();
        
        double avgDataUsage = 15.0;

        List<BenefitDto> benefits = Arrays.asList(
                createBenefitDto("넷플릭스", "PREMIUM"),
                createBenefitDto("아이들나라", "MEDIA")
        );
        when(planApiServiceClient.getBenefitsByPlanId(anyInt())).thenReturn(benefits);
        
        // when
        RecommendationResponseDto result = recommendationCombineAndRanker.combineAndRankRecommendations(
                userSimilarityResults, weightedResults, directSimilarityResults, 
                userPreference, avgDataUsage, null
        );
        
        // then
        assertNotNull(result);
        assertEquals(userPreference, result.getUserPreference());
        assertEquals(avgDataUsage, result.getAvgDataUsage());
        assertEquals(3, result.getRecommendPlans().size());
        
        // 점수 순서대로 정렬되었는지 확인
        assertTrue(result.getRecommendPlans().get(0).getScore() >= result.getRecommendPlans().get(1).getScore());
        assertTrue(result.getRecommendPlans().get(1).getScore() >= result.getRecommendPlans().get(2).getScore());
        
        // 혜택 정보가 설정되었는지 확인
        for (RecommendPlanDto plan : result.getRecommendPlans()) {
            assertNotNull(plan.getBenefits());
            assertEquals(2, plan.getBenefits().size());
        }
    }
    
    @Test
    @DisplayName("추천 결과 조합 및 랭킹 테스트 - 부정적 피드백 있음")
    void 추천_결과_조합_및_랭킹_테스트_부정적_피드백_있음() {
        // given
        List<RecommendPlanDto> userSimilarityResults = Arrays.asList(
                createRecommendPlanDto(1, 0.8, "USER_SIMILARITY"),
                createRecommendPlanDto(2, 0.7, "USER_SIMILARITY")
        );
        
        List<RecommendPlanDto> weightedResults = Arrays.asList(
                createRecommendPlanDto(2, 0.9, "WEIGHTED"),
                createRecommendPlanDto(3, 0.85, "WEIGHTED")
        );
        
        List<RecommendPlanDto> directSimilarityResults = Arrays.asList(
                createRecommendPlanDto(1, 0.75, "DIRECT_SIMILARITY"),
                createRecommendPlanDto(3, 0.7, "DIRECT_SIMILARITY")
        );
        
        UserPreferenceDto userPreference = UserPreferenceDto.builder()
                .preferenceDataUsage(10)
                .preferenceDataUsageUnit("GB")
                .preferencePrice(50000)
                .build();
        
        double avgDataUsage = 15.0;
        
        // 부정적 피드백 (sentimentCode 2: 불만족)
        FeedbackDto feedbackDto = FeedbackDto.builder()
                .sentimentCode(2L)
                .detailCode(1L)
                .build();
        
        // 혜택 정보 모의 설정
        List<BenefitDto> benefits = Arrays.asList(
                createBenefitDto("넷플릭스", "PREMIUM"),
                createBenefitDto("아이들나라", "MEDIA")
        );
        when(planApiServiceClient.getBenefitsByPlanId(anyInt())).thenReturn(benefits);
        
        // when
        RecommendationResponseDto result = recommendationCombineAndRanker.combineAndRankRecommendations(
                userSimilarityResults, weightedResults, directSimilarityResults, 
                userPreference, avgDataUsage, feedbackDto
        );
        
        // then
        assertNotNull(result);
        assertEquals(3, result.getRecommendPlans().size());

        // 결과가 존재하는지만 확인
        assertNotNull(result.getRecommendPlans());
    }

    private RecommendPlanDto createRecommendPlanDto(int planId, double score, String recommendationType) {
        PlanDto planDto = PlanDto.builder()
                .planId(planId)
                .planName("요금제 " + planId)
                .dataAllowance(10)
                .dataAllowanceUnit("GB")
                .monthlyFee(50000)
                .build();
        
        return RecommendPlanDto.builder()
                .plan(planDto)
                .score(score)
                .recommendationType(recommendationType)
                .build();
    }
    
    private BenefitDto createBenefitDto(String name, String type) {
        BenefitDto benefitDto = new BenefitDto();
        benefitDto.setBenefitName(name);
        benefitDto.setBenefitType(type);
        return benefitDto;
    }
}