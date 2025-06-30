package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.dto.PlanBenefitDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.UserDataRecordResponseDto;
import com.comprehensive.eureka.recommend.dto.response.UserPlanRecordResponseDto;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.service.UserPreferenceService;
import com.comprehensive.eureka.recommend.service.util.DataRecordAvgCalculator;
import com.comprehensive.eureka.recommend.service.util.FeatureVectorGenerator;
import com.comprehensive.eureka.recommend.service.util.SimilarityCalculator;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import com.comprehensive.eureka.recommend.util.api.UserApiServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSimilarRecommenderTest {

    @Mock
    private UserPreferenceService userPreferenceService;

    @Mock
    private UserApiServiceClient userApiServiceClient;

    @Mock
    private PlanApiServiceClient planApiServiceClient;

    @Mock
    private DataRecordAvgCalculator dataRecordAvgCalculator;

    @Mock
    private FeatureVectorGenerator featureVectorGenerator;

    @Mock
    private SimilarityCalculator similarityCalculator;

    @InjectMocks
    private UserSimilarRecommender userSimilarRecommender;

    @Test
    @DisplayName("사용자 유사도 기반 추천 테스트 - 정상 케이스")
    void 사용자_유사도_기반_추천_테스트_정상_케이스() {
        // given
        Long targetUserId = 1L;
        UserPreferenceDto targetUserPreference = createUserPreference(targetUserId);
        List<UserDataRecordResponseDto> targetUserHistory = createUserDataRecords(targetUserId);
        List<PlanDto> targetPlans = createTestPlans();

        List<UserPreferenceDto> allPreferences = Arrays.asList(
                targetUserPreference,
                createUserPreference(2L),
                createUserPreference(3L)
        );
        when(userPreferenceService.getAllUserPreferences()).thenReturn(allPreferences);

        when(dataRecordAvgCalculator.calculateAverageDataUsage(any())).thenReturn(5.0);

        when(userApiServiceClient.getUserDataRecords(anyLong())).thenReturn(targetUserHistory);

        double[] targetUserVector = new double[]{1.0, 2.0, 3.0};
        double[] user2Vector = new double[]{1.1, 2.1, 3.1};
        double[] user3Vector = new double[]{1.2, 2.2, 3.2};

        when(featureVectorGenerator.createUserFeatureVector(eq(targetUserPreference), eq(5.0))).thenReturn(targetUserVector);
        when(featureVectorGenerator.createUserFeatureVector(eq(allPreferences.get(1)), eq(5.0))).thenReturn(user2Vector);
        when(featureVectorGenerator.createUserFeatureVector(eq(allPreferences.get(2)), eq(5.0))).thenReturn(user3Vector);

        when(similarityCalculator.calculateEuclideanSimilarity(targetUserVector, user2Vector)).thenReturn(0.9);
        when(similarityCalculator.calculateEuclideanSimilarity(targetUserVector, user3Vector)).thenReturn(0.8);

        List<UserPlanRecordResponseDto> activeUserPlans = Arrays.asList(
                createUserPlanRecord(2L, 101L),
                createUserPlanRecord(3L, 102L)
        );
        when(userApiServiceClient.getActiveUserPlans(anyList())).thenReturn(activeUserPlans);

        List<PlanBenefitDto> planBenefits = Arrays.asList(
                createPlanBenefit(101L, 1),
                createPlanBenefit(102L, 2)
        );
        when(planApiServiceClient.getPlanBenefitsByPlanBenefitIds(anyList())).thenReturn(planBenefits);

        // when
        List<RecommendPlanDto> result = userSimilarRecommender.recommendBySimilarUsers(
                targetUserId, targetUserPreference, targetUserHistory, targetPlans);

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertEquals("USER_SIMILARITY", result.get(0).getRecommendationType());
    }

    @Test
    @DisplayName("사용자 유사도 기반 추천 테스트 - 유사 사용자 없음")
    void 사용자_유사도_기반_추천_테스트_유사_사용자_없음() {
        // given
        Long targetUserId = 1L;
        UserPreferenceDto targetUserPreference = createUserPreference(targetUserId);
        List<UserDataRecordResponseDto> targetUserHistory = createUserDataRecords(targetUserId);
        List<PlanDto> targetPlans = createTestPlans();

        List<UserPreferenceDto> allPreferences = Arrays.asList(targetUserPreference);
        when(userPreferenceService.getAllUserPreferences()).thenReturn(allPreferences);

        // when
        List<RecommendPlanDto> result = userSimilarRecommender.recommendBySimilarUsers(
                targetUserId, targetUserPreference, targetUserHistory, targetPlans);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
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

    private UserPlanRecordResponseDto createUserPlanRecord(Long userId, Long planBenefitId) {
        UserPlanRecordResponseDto record = new UserPlanRecordResponseDto();
        record.setUserId(userId);
        record.setPlanBenefitId(planBenefitId);
        return record;
    }

    private PlanBenefitDto createPlanBenefit(Long planBenefitId, Integer planId) {
        PlanBenefitDto planBenefit = new PlanBenefitDto();
        planBenefit.setPlanBenefitId(planBenefitId);
        planBenefit.setPlanId(planId);
        return planBenefit;
    }
}
