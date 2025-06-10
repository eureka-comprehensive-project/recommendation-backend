package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendationDto;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.util.api.UserApiServiceClient;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BasicRecommender {

    private final UserApiServiceClient userApiServiceClient;

    public List<RecommendationDto> recommendPlansByAge(List<PlanDto> allPlans, Long userId, int age) {
        try {
            String keyword = getKeywordForAge(age);

            return allPlans.stream()
                    .filter(p -> p.getCategory().contains(keyword))
                    .sorted(Comparator.comparing(PlanDto::getPrice).reversed())
                    .limit(3)
                    .map(p -> {
                        return RecommendationDto.builder()
                                .plan(p)
                                .recommendationType("AGE")
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[기본 추천 로직 실패] 요금제 추천 기본 로직이 실패했습니다.", e);
            throw new RecommendationException(ErrorCode.BASIC_RECOMMENDATION_FAILURE);
        }
    }

    private String getKeywordForAge(int age) {
        if (age <= 12) return "키즈";
        if (age <= 19) return "청소년";
        if (age <= 29) return "유스";
        if (age < 65) return "5G 일반";
        return "시니어";
    }
}
