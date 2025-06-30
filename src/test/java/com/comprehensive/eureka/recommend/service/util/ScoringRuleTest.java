package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.RangeConstant;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoringRuleTest {

    @Mock
    private PlanApiServiceClient planApiServiceClient;

    @InjectMocks
    private ScoringRule scoringRule;

    @Test
    @DisplayName("데이터 점수 계산 테스트 - 선호 데이터가 실제 사용량보다 적은 경우")
    void 데이터_점수_계산_테스트_선호_데이터가_실제_사용량보다_적은_경우() {
        // given
        Double preferredData = 10.0;
        Double actualAvgData = 15.0;
        Double planDataLimit = 20.0;

        // when
        double score = scoringRule.calculateDataScore(preferredData, actualAvgData, planDataLimit);

        // then
        assertTrue(score > 0.0);
        assertTrue(score <= 1.0);
    }

    @Test
    @DisplayName("데이터 점수 계산 테스트 - 무제한 데이터 요금제")
    void 데이터_점수_계산_테스트_무제한_데이터_요금제() {
        // given
        Double preferredData = 100.0;
        Double actualAvgData = 120.0;
        Double planDataLimit = RangeConstant.MAX_DATA; // 무제한 데이터

        // when
        double score = scoringRule.calculateDataScore(preferredData, actualAvgData, planDataLimit);

        // then
        assertEquals(1.0, score);
    }

    @Test
    @DisplayName("데이터 점수 계산 테스트 - 요금제 데이터가 부족한 경우")
    void 데이터_점수_계산_테스트_요금제_데이터가_부족한_경우() {
        // given
        Double preferredData = 20.0;
        Double actualAvgData = 25.0;
        Double planDataLimit = 10.0;

        // when
        double score = scoringRule.calculateDataScore(preferredData, actualAvgData, planDataLimit);

        // then
        assertTrue(score < 0.5);
    }

    @Test
    @DisplayName("가격 점수 계산 테스트 - 선호 가격이 요금제 가격보다 높은 경우")
    void 가격_점수_계산_테스트_선호_가격이_요금제_가격보다_높은_경우() {
        // given
        Integer preferredPrice = 60000;
        Integer planPrice = 50000;

        // when
        double score = scoringRule.calculatePriceScore(preferredPrice, planPrice);

        // then
        assertEquals(1.0, score);
    }

    @Test
    @DisplayName("가격 점수 계산 테스트 - 선호 가격이 요금제 가격보다 낮은 경우")
    void 가격_점수_계산_테스트_선호_가격이_요금제_가격보다_낮은_경우() {
        // given
        Integer preferredPrice = 50000;
        Integer planPrice = 70000;

        // when
        double score = scoringRule.calculatePriceScore(preferredPrice, planPrice);

        // then
        assertTrue(score < 1.0);
        assertTrue(score >= 0.1);
    }

    @Test
    @DisplayName("공유 데이터 점수 계산 테스트")
    void 공유_데이터_점수_계산_테스트() {
        // given
        Double preferredSharedData = 5.0;
        Double planSharedData = 10.0;

        // when
        double score = scoringRule.calculateSharedDataScore(preferredSharedData, planSharedData);

        // then
        assertTrue(score > 0.0);
        assertTrue(score <= 1.0);
    }

    @Test
    @DisplayName("혜택 점수 계산 테스트 - 요금제가 선호 혜택 그룹 ID를 가진 경우")
    void 혜택_점수_계산_테스트_요금제가_선호_혜택_그룹_ID를_가진_경우() {
        // given
        Long preferenceBenefitGroupId = 1L;
        PlanDto plan = PlanDto.builder()
                .planId(1)
                .build();

        when(planApiServiceClient.isPlanHasBenefitGroupId(anyInt(), anyLong())).thenReturn(true);

        // when
        double score = scoringRule.calculateBenefitScore(preferenceBenefitGroupId, plan);

        // then
        assertEquals(1.0, score);
    }

    @Test
    @DisplayName("혜택 점수 계산 테스트 - 요금제가 선호 혜택 그룹 ID를 가지지 않은 경우")
    void 혜택_점수_계산_테스트_요금제가_선호_혜택_그룹_ID를_가지지_않은_경우() {
        // given
        Long preferenceBenefitGroupId = 1L;
        PlanDto plan = PlanDto.builder()
                .planId(1)
                .build();

        // 요금제가 선호 혜택 그룹 ID를 가지고 있지 않다고 가정
        when(planApiServiceClient.isPlanHasBenefitGroupId(anyInt(), anyLong())).thenReturn(false);

        // when
        double score = scoringRule.calculateBenefitScore(preferenceBenefitGroupId, plan);

        // then
        assertEquals(0.0, score);
    }

    @Test
    @DisplayName("부가 통화 점수 계산 테스트")
    void 부가_통화_점수_계산_테스트() {
        // given
        Integer preferredValueAddedCallUsage = 100;
        Integer planValueAddedCallAmount = 200;

        // when
        double score = scoringRule.calculateValueAddedCallScore(preferredValueAddedCallUsage, planValueAddedCallAmount);

        // then
        assertTrue(score > 0.0);
        assertTrue(score <= 1.0);
    }

    @Test
    @DisplayName("가족 데이터 점수 계산 테스트 - 둘 다 활성화된 경우")
    void 가족_데이터_점수_계산_테스트_둘_다_활성화된_경우() {
        // given
        boolean isPreferredFamilyData = true;
        boolean isPlanFamilyDataEnabled = true;

        // when
        double score = scoringRule.calculateFamilyDataScore(isPreferredFamilyData, isPlanFamilyDataEnabled);

        // then
        assertEquals(1.0, score);
    }

    @Test
    @DisplayName("가족 데이터 점수 계산 테스트 - 선호는 비활성화, 요금제는 활성화된 경우")
    void 가족_데이터_점수_계산_테스트_선호는_비활성화_요금제는_활성화된_경우() {
        // given
        boolean isPreferredFamilyData = false;
        boolean isPlanFamilyDataEnabled = true;

        // when
        double score = scoringRule.calculateFamilyDataScore(isPreferredFamilyData, isPlanFamilyDataEnabled);

        // then
        assertEquals(0.5, score);
    }

    @Test
    @DisplayName("가족 데이터 점수 계산 테스트 - 요금제가 비활성화된 경우")
    void 가족_데이터_점수_계산_테스트_요금제가_비활성화된_경우() {
        // given
        boolean isPreferredFamilyData = true;
        boolean isPlanFamilyDataEnabled = false;

        // when
        double score = scoringRule.calculateFamilyDataScore(isPreferredFamilyData, isPlanFamilyDataEnabled);

        // then
        assertEquals(0.0, score);
    }
}