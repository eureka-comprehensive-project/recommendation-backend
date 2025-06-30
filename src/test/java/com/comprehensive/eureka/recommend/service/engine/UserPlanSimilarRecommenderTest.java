package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.dto.BenefitDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.UserDataRecordResponseDto;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.service.util.DataRecordAvgCalculator;
import com.comprehensive.eureka.recommend.service.util.FeatureVectorGenerator;
import com.comprehensive.eureka.recommend.service.util.ScoreCalculator;
import com.comprehensive.eureka.recommend.service.util.SimilarityCalculator;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPlanSimilarRecommenderTest {

    @Mock
    private PlanApiServiceClient planApiServiceClient;

    @Mock
    private DataRecordAvgCalculator dataRecordAvgCalculator;

    @Mock
    private FeatureVectorGenerator featureVectorGenerator;

    @Mock
    private SimilarityCalculator similarityCalculator;

    @Mock
    private ScoreCalculator scoreCalculator;

    @InjectMocks
    private UserPlanSimilarRecommender userPlanSimilarRecommender;

    @Test
    @DisplayName("사용자-요금제 유사도 기반 추천 테스트 - 정상 케이스")
    void 사용자_요금제_유사도_기반_추천_테스트_정상_케이스() {
        // given
        UserPreferenceDto userPreference = createUserPreference(1L);
        List<UserDataRecordResponseDto> userHistory = createUserDataRecords(1L);
        List<PlanDto> targetPlans = createTestPlans();

        when(dataRecordAvgCalculator.calculateAverageDataUsage(userHistory)).thenReturn(5.0);

        // Mock feature vector generation
        double[] userVector = new double[]{1.0, 2.0, 3.0};
        double[] plan1Vector = new double[]{1.1, 2.1, 3.1};
        double[] plan2Vector = new double[]{1.2, 2.2, 3.2};
        
        when(featureVectorGenerator.createUserFeatureVector(userPreference, 5.0)).thenReturn(userVector);
        when(featureVectorGenerator.createPlanFeatureVector(targetPlans.get(0))).thenReturn(plan1Vector);
        when(featureVectorGenerator.createPlanFeatureVector(targetPlans.get(1))).thenReturn(plan2Vector);

        List<BenefitDto> benefits = createTestBenefits();
        when(planApiServiceClient.getBenefitsByPlanId(anyInt())).thenReturn(benefits);

        when(similarityCalculator.calculateEuclideanSimilarity(userVector, plan1Vector)).thenReturn(0.9);
        when(similarityCalculator.calculateEuclideanSimilarity(userVector, plan2Vector)).thenReturn(0.8);
        when(similarityCalculator.calculateUserPlanBenefitSimilarity(any(), any())).thenReturn(0.7);

        when(scoreCalculator.calculateSufficiencyScore(any(), any())).thenReturn(0.9);

        // when
        List<RecommendPlanDto> result = userPlanSimilarRecommender.recommendByUserPlanSimilarity(
                userPreference, userHistory, targetPlans);

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        assertTrue(result.get(0).getScore() >= result.get(1).getScore());

        assertEquals("USER_PLAN_SIMILARITY", result.get(0).getRecommendationType());
    }


    @Test
    @DisplayName("사용자-요금제 유사도 기반 추천 테스트 - 일부 요금제 처리 실패")
    void 사용자_요금제_유사도_기반_추천_테스트_일부_요금제_처리_실패() {
        // given
        UserPreferenceDto userPreference = createUserPreference(1L);
        List<UserDataRecordResponseDto> userHistory = createUserDataRecords(1L);
        List<PlanDto> targetPlans = createTestPlans();

        when(dataRecordAvgCalculator.calculateAverageDataUsage(userHistory)).thenReturn(5.0);

        double[] userVector = new double[]{1.0, 2.0, 3.0};
        double[] plan1Vector = new double[]{1.1, 2.1, 3.1};
        
        when(featureVectorGenerator.createUserFeatureVector(userPreference, 5.0)).thenReturn(userVector);
        when(featureVectorGenerator.createPlanFeatureVector(targetPlans.get(0))).thenReturn(plan1Vector);

        when(featureVectorGenerator.createPlanFeatureVector(targetPlans.get(1))).thenThrow(new RuntimeException("특성 벡터 생성 실패"));

        List<BenefitDto> benefits = createTestBenefits();
        when(planApiServiceClient.getBenefitsByPlanId(anyInt())).thenReturn(benefits);

        when(similarityCalculator.calculateEuclideanSimilarity(userVector, plan1Vector)).thenReturn(0.9);
        when(similarityCalculator.calculateUserPlanBenefitSimilarity(any(), any())).thenReturn(0.7);

        when(scoreCalculator.calculateSufficiencyScore(any(), any())).thenReturn(0.9);

        // when
        List<RecommendPlanDto> result = userPlanSimilarRecommender.recommendByUserPlanSimilarity(
                userPreference, userHistory, targetPlans);

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    private UserPreferenceDto createUserPreference(Long userId) {
        return UserPreferenceDto.builder()
                .userId(userId)
                .preferenceBenefitGroupId(1L)
                .preferenceDataUsage(5)
                .preferenceDataUsageUnit("GB")
                .preferencePrice(50000)
                .build();
    }

    private List<UserDataRecordResponseDto> createUserDataRecords(Long userId) {
        return Arrays.asList(
                UserDataRecordResponseDto.builder()
                        .userId(userId)
                        .dataUsage(5)
                        .dataUsageUnit("GB")
                        .yearMonth("202301")
                        .build(),
                UserDataRecordResponseDto.builder()
                        .userId(userId)
                        .dataUsage(6)
                        .dataUsageUnit("GB")
                        .yearMonth("202302")
                        .build()
        );
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
                        .build()
        );
    }

    private List<BenefitDto> createTestBenefits() {
        BenefitDto benefit1 = new BenefitDto();
        benefit1.setBenefitName("넷플릭스");
        benefit1.setBenefitType("PREMIUM");
        
        BenefitDto benefit2 = new BenefitDto();
        benefit2.setBenefitName("아이들나라");
        benefit2.setBenefitType("MEDIA");
        
        return Arrays.asList(benefit1, benefit2);
    }
}