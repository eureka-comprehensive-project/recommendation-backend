package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.dto.BenefitDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.RecommendationResponseDto;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasicRecommenderTest {

    @Mock
    private UserApiServiceClient userApiServiceClient;

    @Mock
    private PlanApiServiceClient planApiServiceClient;

    @InjectMocks
    private BasicRecommender basicRecommender;

    @Test
    @DisplayName("연령대별 요금제 추천 테스트 - 청소년")
    void 연령대별_요금제_추천_테스트_청소년() {
        // given
        List<PlanDto> allPlans = createTestPlans();
        UserPreferenceDto userPreference = createUserPreference();
        Double avgDataUsage = 5.0;
        int age = 15;

        when(planApiServiceClient.getBenefitsByPlanId(anyInt())).thenReturn(createTestBenefits());

        // when
        RecommendationResponseDto result = basicRecommender.recommendPlansByAge(allPlans, userPreference, avgDataUsage, age);

        // then
        assertNotNull(result);
        assertNotNull(result.getRecommendPlans());
        assertFalse(result.getRecommendPlans().isEmpty());

        boolean hasYouthPlan = result.getRecommendPlans().stream()
                .anyMatch(plan -> plan.getPlan().getPlanCategory().contains("청소년"));
        assertTrue(hasYouthPlan);

        assertEquals("AGE", result.getRecommendPlans().get(0).getRecommendationType());
    }

    @Test
    @DisplayName("연령대별 요금제 추천 테스트 - 유스")
    void 연령대별_요금제_추천_테스트_유스() {
        // given
        List<PlanDto> allPlans = createTestPlans();
        UserPreferenceDto userPreference = createUserPreference();
        Double avgDataUsage = 5.0;
        int age = 25;

        when(planApiServiceClient.getBenefitsByPlanId(anyInt())).thenReturn(createTestBenefits());

        // when
        RecommendationResponseDto result = basicRecommender.recommendPlansByAge(allPlans, userPreference, avgDataUsage, age);

        // then
        assertNotNull(result);
        assertNotNull(result.getRecommendPlans());
        assertFalse(result.getRecommendPlans().isEmpty());

        boolean hasYouthPlan = result.getRecommendPlans().stream()
                .anyMatch(plan -> plan.getPlan().getPlanCategory().contains("유스"));
        assertTrue(hasYouthPlan);
    }

    @Test
    @DisplayName("연령대별 요금제 추천 테스트 - 프리미엄")
    void 연령대별_요금제_추천_테스트_프리미엄() {
        // given
        List<PlanDto> allPlans = createTestPlans();
        UserPreferenceDto userPreference = createUserPreference();
        Double avgDataUsage = 5.0;
        int age = 40;

        when(planApiServiceClient.getBenefitsByPlanId(anyInt())).thenReturn(createTestBenefits());

        // when
        RecommendationResponseDto result = basicRecommender.recommendPlansByAge(allPlans, userPreference, avgDataUsage, age);

        // then
        assertNotNull(result);
        assertNotNull(result.getRecommendPlans());
        assertFalse(result.getRecommendPlans().isEmpty());

        boolean hasPremiumPlan = result.getRecommendPlans().stream()
                .anyMatch(plan -> plan.getPlan().getPlanCategory().contains("프리미엄"));
        assertTrue(hasPremiumPlan);
    }

    @Test
    @DisplayName("연령대별 요금제 추천 테스트 - 시니어")
    void 연령대별_요금제_추천_테스트_시니어() {
        // given
        List<PlanDto> allPlans = createTestPlans();
        UserPreferenceDto userPreference = createUserPreference();
        Double avgDataUsage = 5.0;
        int age = 70;

        when(planApiServiceClient.getBenefitsByPlanId(anyInt())).thenReturn(createTestBenefits());

        // when
        RecommendationResponseDto result = basicRecommender.recommendPlansByAge(allPlans, userPreference, avgDataUsage, age);

        // then
        assertNotNull(result);
        assertNotNull(result.getRecommendPlans());
        assertFalse(result.getRecommendPlans().isEmpty());

        boolean hasSeniorPlan = result.getRecommendPlans().stream()
                .anyMatch(plan -> plan.getPlan().getPlanCategory().contains("시니어"));
        assertTrue(hasSeniorPlan);
    }

    @Test
    @DisplayName("외부 API 호출 실패 시 예외 발생 테스트")
    void 외부_API_호출_실패_시_예외_발생_테스트() {
        // given
        List<PlanDto> allPlans = createTestPlans();
        UserPreferenceDto userPreference = createUserPreference();
        Double avgDataUsage = 5.0;
        int age = 40;

        when(planApiServiceClient.getBenefitsByPlanId(anyInt())).thenThrow(new RuntimeException("API 호출 실패"));

        // when & then
        assertThrows(RecommendationException.class, () -> 
            basicRecommender.recommendPlansByAge(allPlans, userPreference, avgDataUsage, age)
        );
    }

    private List<PlanDto> createTestPlans() {
        return Arrays.asList(
            PlanDto.builder()
                .planId(1)
                .planName("청소년 요금제")
                .planCategory("청소년")
                .monthlyFee(30000)
                .build(),
            PlanDto.builder()
                .planId(2)
                .planName("유스 요금제")
                .planCategory("유스")
                .monthlyFee(40000)
                .build(),
            PlanDto.builder()
                .planId(3)
                .planName("프리미엄 요금제 1")
                .planCategory("프리미엄")
                .monthlyFee(60000)
                .build(),
            PlanDto.builder()
                .planId(4)
                .planName("프리미엄 요금제 2")
                .planCategory("프리미엄")
                .monthlyFee(70000)
                .build(),
            PlanDto.builder()
                .planId(5)
                .planName("시니어 요금제")
                .planCategory("시니어")
                .monthlyFee(25000)
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

    private List<BenefitDto> createTestBenefits() {
        BenefitDto benefit1 = new BenefitDto();
        benefit1.setBenefitName("데이터 무제한");
        benefit1.setBenefitType("DATA");

        BenefitDto benefit2 = new BenefitDto();
        benefit2.setBenefitName("통화 무제한");
        benefit2.setBenefitType("CALL");

        return Arrays.asList(benefit1, benefit2);
    }
}
