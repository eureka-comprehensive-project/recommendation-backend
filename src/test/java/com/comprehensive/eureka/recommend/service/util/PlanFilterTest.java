package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.CategoryConstant;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PlanFilterTest {

    @InjectMocks
    private PlanFilter planFilter;

    @Test
    @DisplayName("나이에 따른 요금제 필터링 테스트 - 10대")
    void 나이에_따른_요금제_필터링_테스트_10대() {
        // given
        List<PlanDto> allPlans = Arrays.asList(
                createPlan(1, CategoryConstant.CATEGORY_TEEN),
                createPlan(2, CategoryConstant.CATEGORY_YOUTH),
                createPlan(3, CategoryConstant.CATEGORY_PREMIUM),
                createPlan(4, CategoryConstant.CATEGORY_SENIOR)
        );
        int userAge = 15;

        // when
        List<PlanDto> filteredPlans = planFilter.filterPlansByAge(allPlans, userAge);

        // then
        assertEquals(2, filteredPlans.size());
        assertTrue(filteredPlans.stream().anyMatch(plan -> plan.getPlanId() == 1)); // TEEN
        assertTrue(filteredPlans.stream().anyMatch(plan -> plan.getPlanId() == 3)); // PREMIUM
    }

    @Test
    @DisplayName("나이에 따른 요금제 필터링 테스트 - 20대")
    void 나이에_따른_요금제_필터링_테스트_20대() {
        // given
        List<PlanDto> allPlans = Arrays.asList(
                createPlan(1, CategoryConstant.CATEGORY_TEEN),
                createPlan(2, CategoryConstant.CATEGORY_YOUTH),
                createPlan(3, CategoryConstant.CATEGORY_PREMIUM),
                createPlan(4, CategoryConstant.CATEGORY_SENIOR)
        );
        int userAge = 25;

        // when
        List<PlanDto> filteredPlans = planFilter.filterPlansByAge(allPlans, userAge);

        // then
        assertEquals(2, filteredPlans.size());
        assertTrue(filteredPlans.stream().anyMatch(plan -> plan.getPlanId() == 2)); // YOUTH
        assertTrue(filteredPlans.stream().anyMatch(plan -> plan.getPlanId() == 3)); // PREMIUM
    }

    @Test
    @DisplayName("나이에 따른 요금제 필터링 테스트 - 30~64세")
    void 나이에_따른_요금제_필터링_테스트_30_64세() {
        // given
        List<PlanDto> allPlans = Arrays.asList(
                createPlan(1, CategoryConstant.CATEGORY_TEEN),
                createPlan(2, CategoryConstant.CATEGORY_YOUTH),
                createPlan(3, CategoryConstant.CATEGORY_PREMIUM),
                createPlan(4, CategoryConstant.CATEGORY_SENIOR)
        );
        int userAge = 40;

        // when
        List<PlanDto> filteredPlans = planFilter.filterPlansByAge(allPlans, userAge);

        // then
        assertEquals(1, filteredPlans.size());
        assertTrue(filteredPlans.stream().anyMatch(plan -> plan.getPlanId() == 3)); // PREMIUM
    }

    @Test
    @DisplayName("나이에 따른 요금제 필터링 테스트 - 65세 이상")
    void 나이에_따른_요금제_필터링_테스트_65세_이상() {
        // given
        List<PlanDto> allPlans = Arrays.asList(
                createPlan(1, CategoryConstant.CATEGORY_TEEN),
                createPlan(2, CategoryConstant.CATEGORY_YOUTH),
                createPlan(3, CategoryConstant.CATEGORY_PREMIUM),
                createPlan(4, CategoryConstant.CATEGORY_SENIOR)
        );
        int userAge = 70;

        // when
        List<PlanDto> filteredPlans = planFilter.filterPlansByAge(allPlans, userAge);

        // then
        assertEquals(2, filteredPlans.size());
        assertTrue(filteredPlans.stream().anyMatch(plan -> plan.getPlanId() == 3)); // PREMIUM
        assertTrue(filteredPlans.stream().anyMatch(plan -> plan.getPlanId() == 4)); // SENIOR
    }

    @Test
    @DisplayName("나이에 따른 요금제 필터링 테스트 - 빈 리스트")
    void 나이에_따른_요금제_필터링_테스트_빈_리스트() {
        // given
        List<PlanDto> allPlans = Collections.emptyList();
        int userAge = 30;

        // when
        List<PlanDto> filteredPlans = planFilter.filterPlansByAge(allPlans, userAge);

        // then
        assertTrue(filteredPlans.isEmpty());
    }

    @Test
    @DisplayName("나이에 따른 요금제 필터링 테스트 - 카테고리가 null인 요금제")
    void 나이에_따른_요금제_필터링_테스트_카테고리가_null인_요금제() {
        // given
        List<PlanDto> allPlans = Arrays.asList(
                createPlan(1, CategoryConstant.CATEGORY_TEEN),
                createPlan(2, null),
                createPlan(3, CategoryConstant.CATEGORY_PREMIUM)
        );
        int userAge = 30;

        // when
        List<PlanDto> filteredPlans = planFilter.filterPlansByAge(allPlans, userAge);

        // then
        assertEquals(1, filteredPlans.size());
        assertTrue(filteredPlans.stream().anyMatch(plan -> plan.getPlanId() == 3)); // PREMIUM
    }

    private PlanDto createPlan(int planId, String category) {
        return PlanDto.builder()
                .planId(planId)
                .planCategory(category)
                .build();
    }
}