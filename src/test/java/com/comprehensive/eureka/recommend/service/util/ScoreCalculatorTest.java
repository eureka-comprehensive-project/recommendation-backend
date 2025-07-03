package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.dto.FeedbackDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.entity.enums.DataPeriod;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreCalculatorTest {

    @Mock
    private ScoringRule scoringRule;

    @Mock
    private PlanApiServiceClient planApiServiceClient;

    @InjectMocks
    private ScoreCalculator scoreCalculator;

    @Test
    @DisplayName("가중치 점수 계산 테스트 - 데이터 관련 피드백 있음")
    void 가중치_점수_계산_테스트_데이터_관련_피드백_있음() {
        // given
        UserPreferenceDto userPref = UserPreferenceDto.builder()
                .preferenceDataUsage(10)
                .preferenceDataUsageUnit("GB")
                .preferencePrice(50000)
                .preferenceSharedDataUsage(5)
                .preferenceSharedDataUsageUnit("GB")
                .build();

        PlanDto plan = PlanDto.builder()
                .dataAllowance(20)
                .dataAllowanceUnit("GB")
                .dataPeriod(DataPeriod.MONTH)
                .monthlyFee(60000)
                .tetheringDataAmount(10)
                .tetheringDataUnit("GB")
                .build();

        double avgDataUsage = 15.0;

        // 데이터 관련 피드백 (detailCode 1: 데이터 부족)
        FeedbackDto feedbackDto = FeedbackDto.builder()
                .sentimentCode(2L)
                .detailCode(1L)
                .build();

        // 각 점수 계산 결과 모의 설정
        when(scoringRule.calculateDataScore(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.8);
        when(scoringRule.calculatePriceScore(anyInt(), anyInt())).thenReturn(0.7);
        when(scoringRule.calculateSharedDataScore(anyDouble(), anyDouble())).thenReturn(0.9);

        // when
        double score = scoreCalculator.calculateWeightedScore(userPref, avgDataUsage, plan, feedbackDto);

        // then
        assertTrue(score > 0);
    }

    @Test
    @DisplayName("충분성 점수 계산 테스트 - 데이터 충분")
    void 충분성_점수_계산_테스트_데이터_충분() {
        // given
        UserPreferenceDto userPref = UserPreferenceDto.builder()
                .preferenceDataUsage(10)
                .preferenceDataUsageUnit("GB")
                .preferencePrice(50000)
                .build();

        PlanDto plan = PlanDto.builder()
                .dataAllowance(20)
                .dataAllowanceUnit("GB")
                .dataPeriod(DataPeriod.MONTH)
                .monthlyFee(50000)
                .build();

        // when
        double score = scoreCalculator.calculateSufficiencyScore(plan, userPref);

        // then
        assertTrue(score > 0.5);
    }

    @Test
    @DisplayName("충분성 점수 계산 테스트 - 데이터 부족")
    void 충분성_점수_계산_테스트_데이터_부족() {
        // given
        UserPreferenceDto userPref = UserPreferenceDto.builder()
                .preferenceDataUsage(20)
                .preferenceDataUsageUnit("GB")
                .preferencePrice(50000)
                .build();

        PlanDto plan = PlanDto.builder()
                .dataAllowance(5)
                .dataAllowanceUnit("GB")
                .dataPeriod(DataPeriod.MONTH)
                .monthlyFee(50000)
                .build();

        // when
        double score = scoreCalculator.calculateSufficiencyScore(plan, userPref);

        // then
        assertTrue(score < 0.5);
    }

    @Test
    @DisplayName("충분성 점수 계산 테스트 - 가격 초과")
    void 충분성_점수_계산_테스트_가격_초과() {
        // given
        UserPreferenceDto userPref = UserPreferenceDto.builder()
                .preferenceDataUsage(10)
                .preferenceDataUsageUnit("GB")
                .preferencePrice(50000)
                .build();

        PlanDto plan = PlanDto.builder()
                .dataAllowance(10)
                .dataAllowanceUnit("GB")
                .dataPeriod(DataPeriod.MONTH)
                .monthlyFee(80000)
                .build();

        // when
        double score = scoreCalculator.calculateSufficiencyScore(plan, userPref);

        // then
        assertTrue(score < 1.0);
    }
}
