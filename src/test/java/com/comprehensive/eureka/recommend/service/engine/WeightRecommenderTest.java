package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.dto.FeedbackDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.UserDataRecordResponseDto;
import com.comprehensive.eureka.recommend.service.util.DataRecordAvgCalculator;
import com.comprehensive.eureka.recommend.service.util.ScoreCalculator;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeightRecommenderTest {

    @Mock
    private DataRecordAvgCalculator dataRecordAvgCalculator;

    @Mock
    private ScoreCalculator scoreCalculator;

    @InjectMocks
    private WeightRecommender weightRecommender;

    @Test
    @DisplayName("가중치 점수 기반 추천 테스트 - 정상 케이스")
    void 가중치_점수_기반_추천_테스트_정상_케이스() {
        // given
        UserPreferenceDto userPreference = createUserPreference(1L);
        List<PlanDto> targetPlans = createTestPlans();
        List<UserDataRecordResponseDto> userHistory = createUserDataRecords(1L);
        FeedbackDto feedbackDto = createFeedbackDto();

        when(dataRecordAvgCalculator.calculateAverageDataUsage(userHistory)).thenReturn(5.0);

        when(scoreCalculator.calculateWeightedScore(eq(userPreference), eq(5.0), eq(targetPlans.get(0)), eq(feedbackDto))).thenReturn(0.9);
        when(scoreCalculator.calculateWeightedScore(eq(userPreference), eq(5.0), eq(targetPlans.get(1)), eq(feedbackDto))).thenReturn(0.8);

        // when
        List<RecommendPlanDto> result = weightRecommender.recommendByWeightScore(
                userPreference, targetPlans, userHistory, feedbackDto);

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        assertTrue(result.get(0).getScore() >= result.get(1).getScore());

        assertEquals("WEIGHTED", result.get(0).getRecommendationType());
    }

    @Test
    @DisplayName("가중치 점수 기반 추천 테스트 - 동일 점수")
    void 가중치_점수_기반_추천_테스트_동일_점수() {
        // given
        UserPreferenceDto userPreference = createUserPreference(1L);
        List<PlanDto> targetPlans = createTestPlans();
        List<UserDataRecordResponseDto> userHistory = createUserDataRecords(1L);
        FeedbackDto feedbackDto = createFeedbackDto();

        when(dataRecordAvgCalculator.calculateAverageDataUsage(userHistory)).thenReturn(5.0);

        when(scoreCalculator.calculateWeightedScore(eq(userPreference), eq(5.0), eq(targetPlans.get(0)), eq(feedbackDto))).thenReturn(0.9);
        when(scoreCalculator.calculateWeightedScore(eq(userPreference), eq(5.0), eq(targetPlans.get(1)), eq(feedbackDto))).thenReturn(0.9);

        // when
        List<RecommendPlanDto> result = weightRecommender.recommendByWeightScore(
                userPreference, targetPlans, userHistory, feedbackDto);

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        assertEquals(result.get(0).getScore(), result.get(1).getScore());
    }

    @Test
    @DisplayName("가중치 점수 기반 추천 테스트 - 빈 요금제 목록")
    void 가중치_점수_기반_추천_테스트_빈_요금제_목록() {
        // given
        UserPreferenceDto userPreference = createUserPreference(1L);
        List<PlanDto> emptyPlans = Arrays.asList();
        List<UserDataRecordResponseDto> userHistory = createUserDataRecords(1L);
        FeedbackDto feedbackDto = createFeedbackDto();

        when(dataRecordAvgCalculator.calculateAverageDataUsage(userHistory)).thenReturn(5.0);

        // when
        List<RecommendPlanDto> result = weightRecommender.recommendByWeightScore(
                userPreference, emptyPlans, userHistory, feedbackDto);

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

    private FeedbackDto createFeedbackDto() {
        return FeedbackDto.builder()
                .keyword("데이터")
                .sentimentCode(1L)
                .detailCode(101L)
                .build();
    }
}
