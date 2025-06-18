package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.dto.BenefitDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.RecommendationResponseDto;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import com.comprehensive.eureka.recommend.util.api.UserApiServiceClient;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BasicRecommender {

    private final UserApiServiceClient userApiServiceClient;
    private final PlanApiServiceClient planApiServiceClient;

    public RecommendationResponseDto recommendPlansByAge(
            List<PlanDto> allPlans,
            UserPreferenceDto userPreference,
            Double avgDataUsage,
            int age
    ) {
        try {
            String keyword = getKeywordForAge(age);

            List<PlanDto> ageSpecificPlans = allPlans.stream()
                    .filter(p -> p.getPlanCategory() != null && p.getPlanCategory().contains(keyword))
                    .sorted(Comparator.comparing(PlanDto::getMonthlyFee).reversed())
                    .limit(1)
                    .toList();

            List<PlanDto> premiumPlans = allPlans.stream()
                    .filter(p -> p.getPlanCategory() != null && p.getPlanCategory().contains("프리미엄"))
                    .sorted(Comparator.comparing(PlanDto::getMonthlyFee).reversed())
                    .limit(2)
                    .toList();

            List<PlanDto> combinedPlans = Stream.concat(ageSpecificPlans.stream(), premiumPlans.stream())
                    .distinct()
                    .limit(3)
                    .toList();

            List<RecommendPlanDto> recommendPlans = combinedPlans.stream()
                    .map(p -> RecommendPlanDto.builder()
                            .plan(p)
                            .recommendationType("AGE")
                            .build())
                    .peek(recommendPlanDto -> {
                        List<BenefitDto> benefits = fetchBenefitsByPlanId(recommendPlanDto.getPlan().getPlanId());
                        recommendPlanDto.setBenefits(benefits);
                    })
                    .collect(Collectors.toList());

            return RecommendationResponseDto.builder()
                    .userPreference(userPreference)
                    .avgDataUsage(avgDataUsage)
                    .recommendPlans(recommendPlans)
                    .build();

        } catch (Exception e) {
            log.error("[기본 추천 로직 실패] 요금제 추천 기본 로직이 실패했습니다.", e);
            throw new RecommendationException(ErrorCode.BASIC_RECOMMENDATION_FAILURE);
        }
    }

    private String getKeywordForAge(int age) {
        if (age <= 19) return "청소년";
        if (age <= 29) return "유스";
        if (age < 65) return "프리미엄";
        return "시니어";
    }

    private List<BenefitDto> fetchBenefitsByPlanId(Integer planId) {
        try {
            return planApiServiceClient.getBenefitsByPlanId(planId);

        } catch (Exception e) {
            log.error("[외부 API 호출 실패] 요금제 혜택 정보 호출에 실패했습니다.", e);
            throw new RecommendationException(ErrorCode.PLAN_BENEFIT_LOAD_FAILURE);
        }
    }
}
