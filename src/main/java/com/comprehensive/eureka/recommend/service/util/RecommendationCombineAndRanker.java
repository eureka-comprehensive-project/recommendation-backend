package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.WeightConstant;
import com.comprehensive.eureka.recommend.dto.BenefitDto;
import com.comprehensive.eureka.recommend.dto.FeedbackDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.RecommendationResponseDto;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationCombineAndRanker {

    private final PlanApiServiceClient planApiServiceClient;

    public RecommendationResponseDto combineAndRankRecommendations(
            List<RecommendPlanDto> userSimilarityResults,
            List<RecommendPlanDto> weightedResults,
            List<RecommendPlanDto> directSimilarityResults,
            UserPreferenceDto userPreference,
            double avgDataUsage,
            FeedbackDto feedbackDto
    ) {
        log.info("추천 결과 조합 및 최종 추천 결과 생성 시작");
        double weightedRecommendWeight = WeightConstant.WEIGHT_SCORE_WEIGHT;
        double userSimilarityWeight = WeightConstant.USER_SIMILARITY_SCORE_WEIGHT;
        double directSimilarityWeight = WeightConstant.USER_PLAN_SIMILARITY_WEIGHT;

        if (feedbackDto != null && (feedbackDto.getSentimentCode() == 2 || feedbackDto.getSentimentCode() == 3)) {
            log.info("사용자 피드백에 따라 추천 결과 최종 가중치를 조정합니다.");
            weightedRecommendWeight += 0.4;
            userSimilarityWeight -= 0.4;
            directSimilarityWeight -= 0.4;
        }

        try {
            Map<Integer, RecommendPlanDto> combinedResults = new HashMap<>();

            double finalUserSimilarityWeight = userSimilarityWeight;
            userSimilarityResults.forEach(result -> {
                result.setScore(result.getScore() * finalUserSimilarityWeight);
                combinedResults.put(result.getPlan().getPlanId(), result);
            });
            log.debug("사용자 유사도 기반 추천 결과 처리");

            double finalWeightedRecommendWeight = weightedRecommendWeight;
            weightedResults.forEach(result -> {
                Integer planId = result.getPlan().getPlanId();
                double weightedScore = result.getScore() * finalWeightedRecommendWeight;

                combinedResults.merge(planId, result, (existing, newResult) -> {
                    existing.setScore(existing.getScore() + weightedScore);
                    existing.setRecommendationType("HYBRID");
                    return existing;
                });
            });
            log.debug("가중치 기반 추천 결과 처리");

            double finalDirectSimilarityWeight = directSimilarityWeight;
            directSimilarityResults.forEach(result -> {
                Integer planId = result.getPlan().getPlanId();
                double directSimilarityScore = result.getScore() * finalDirectSimilarityWeight;

                combinedResults.merge(planId, result, (existing, newResult) -> {
                    existing.setScore(existing.getScore() + directSimilarityScore);
                    existing.setRecommendationType("HYBRID");
                    return existing;
                });
            });
            log.debug("사용자 - 요금제 간 유사도 기반 추천 결과 처리");

            List<RecommendPlanDto> finalRecommendations = combinedResults.values().stream()
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(3)
                    .peek(recommendPlanDto -> {
                        List<BenefitDto> benefits = fetchBenefitsByPlanId(recommendPlanDto.getPlan().getPlanId());
                        recommendPlanDto.setBenefits(benefits);
                    })
                    .collect(Collectors.toList());

            log.info("추천 결과 조합 및 최종 결과 생성 완료");
            return RecommendationResponseDto.builder()
                    .userPreference(userPreference)
                    .avgDataUsage(avgDataUsage)
                    .recommendPlans(finalRecommendations)
                    .build();

        } catch (Exception e) {
            log.error("추천 결과 조합 및 최종 결과 생성 중 오류가 발생했습니다.", e);
            throw new RecommendationException(ErrorCode.COMBINE_AND_RANK_RECOMMENDATION_FAILURE);
        }
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