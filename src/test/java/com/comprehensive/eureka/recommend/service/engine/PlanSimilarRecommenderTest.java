package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.RecommendationResponseDto;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.service.util.FeatureVectorGenerator;
import com.comprehensive.eureka.recommend.service.util.SimilarityCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanSimilarRecommenderTest {

    @Mock
    private FeatureVectorGenerator featureVectorGenerator;

    @Mock
    private SimilarityCalculator similarityCalculator;

    @InjectMocks
    private PlanSimilarRecommender planSimilarRecommender;

    @Test
    @DisplayName("요금제 유사도 기반 추천 테스트 - 정상 케이스")
    void 요금제_유사도_기반_추천_테스트_정상_케이스() {
        // given
        List<PlanDto> targetPlans = createTestPlans();
        PlanDto targetPlan = targetPlans.get(0);
        double avgDataUsage = 5.0;
        UserPreferenceDto userPreference = createUserPreference();

        double[] targetPlanVector = new double[]{1.0, 2.0, 3.0};
        double[] plan1Vector = new double[]{1.1, 2.1, 3.1};
        double[] plan2Vector = new double[]{1.2, 2.2, 3.2};
        double[] plan3Vector = new double[]{1.3, 2.3, 3.3};

        when(featureVectorGenerator.createPlanFeatureVector(targetPlan)).thenReturn(targetPlanVector);
        when(featureVectorGenerator.createPlanFeatureVector(targetPlans.get(1))).thenReturn(plan1Vector);
        when(featureVectorGenerator.createPlanFeatureVector(targetPlans.get(2))).thenReturn(plan2Vector);
        when(featureVectorGenerator.createPlanFeatureVector(targetPlans.get(3))).thenReturn(plan3Vector);

        when(similarityCalculator.calculateEuclideanSimilarity(targetPlanVector, plan1Vector)).thenReturn(0.9);
        when(similarityCalculator.calculateEuclideanSimilarity(targetPlanVector, plan2Vector)).thenReturn(0.8);
        when(similarityCalculator.calculateEuclideanSimilarity(targetPlanVector, plan3Vector)).thenReturn(0.7);

        // when
        RecommendationResponseDto result = planSimilarRecommender.recommendByPlanSimilarity(
                targetPlans, targetPlan, avgDataUsage, userPreference);

        // then
        assertNotNull(result);
        assertNotNull(result.getRecommendPlans());
        assertEquals(3, result.getRecommendPlans().size());

        assertTrue(result.getRecommendPlans().get(0).getScore() >= result.getRecommendPlans().get(1).getScore());
        assertTrue(result.getRecommendPlans().get(1).getScore() >= result.getRecommendPlans().get(2).getScore());

        assertEquals("PLAN_SIMILARITY", result.getRecommendPlans().get(0).getRecommendationType());
    }

    @Test
    @DisplayName("요금제 유사도 기반 추천 테스트 - 예외 발생")
    void 요금제_유사도_기반_추천_테스트_예외_발생() {
        // given
        List<PlanDto> targetPlans = createTestPlans();
        PlanDto targetPlan = targetPlans.get(0);
        double avgDataUsage = 5.0;
        UserPreferenceDto userPreference = createUserPreference();

        when(featureVectorGenerator.createPlanFeatureVector(any())).thenThrow(new RuntimeException("특성 벡터 생성 실패"));

        // when & then
        assertThrows(RecommendationException.class, () -> 
            planSimilarRecommender.recommendByPlanSimilarity(targetPlans, targetPlan, avgDataUsage, userPreference)
        );
    }

    @Test
    @DisplayName("요금제 유사도 기반 추천 테스트 - 일부 요금제 처리 실패")
    void 요금제_유사도_기반_추천_테스트_일부_요금제_처리_실패() {
        // given
        List<PlanDto> targetPlans = createTestPlans();
        PlanDto targetPlan = targetPlans.get(0);
        double avgDataUsage = 5.0;
        UserPreferenceDto userPreference = createUserPreference();

        double[] targetPlanVector = new double[]{1.0, 2.0, 3.0};
        double[] plan1Vector = new double[]{1.1, 2.1, 3.1};
        double[] plan3Vector = new double[]{1.3, 2.3, 3.3};

        when(featureVectorGenerator.createPlanFeatureVector(targetPlan)).thenReturn(targetPlanVector);
        when(featureVectorGenerator.createPlanFeatureVector(targetPlans.get(1))).thenReturn(plan1Vector);

        when(featureVectorGenerator.createPlanFeatureVector(targetPlans.get(2))).thenThrow(new RuntimeException("특성 벡터 생성 실패"));
        when(featureVectorGenerator.createPlanFeatureVector(targetPlans.get(3))).thenReturn(plan3Vector);

        when(similarityCalculator.calculateEuclideanSimilarity(targetPlanVector, plan1Vector)).thenReturn(0.9);
        when(similarityCalculator.calculateEuclideanSimilarity(targetPlanVector, plan3Vector)).thenReturn(0.7);

        // when
        RecommendationResponseDto result = planSimilarRecommender.recommendByPlanSimilarity(
                targetPlans, targetPlan, avgDataUsage, userPreference);

        // then
        assertNotNull(result);
        assertNotNull(result.getRecommendPlans());

        assertEquals(2, result.getRecommendPlans().size());
    }

    private List<PlanDto> createTestPlans() {
        return Arrays.asList(
            PlanDto.builder()
                .planId(1)
                .planName("5G 요금제 1")
                .planCategory("5G")
                .monthlyFee(50000)
                .build(),
            PlanDto.builder()
                .planId(2)
                .planName("5G 요금제 2")
                .planCategory("5G")
                .monthlyFee(60000)
                .build(),
            PlanDto.builder()
                .planId(3)
                .planName("LTE 요금제 1")
                .planCategory("LTE")
                .monthlyFee(40000)
                .build(),
            PlanDto.builder()
                .planId(4)
                .planName("LTE 요금제 2")
                .planCategory("LTE")
                .monthlyFee(30000)
                .build()
        );
    }

    private UserPreferenceDto createUserPreference() {
        return UserPreferenceDto.builder()
                .userId(1L)
                .preferenceBenefitGroupId(1L)
                .preferenceDataUsage(5)
                .preferenceDataUsageUnit("GB")
                .preferencePrice(50000)
                .build();
    }
}