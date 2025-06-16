package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.RecommendationResponseDto;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.service.util.FeatureVectorGenerator;
import com.comprehensive.eureka.recommend.service.util.SimilarityCalculator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanSimilarRecommender {

    private final FeatureVectorGenerator featureVectorGenerator;
    private final SimilarityCalculator similarityCalculator;

    private record PlanSimilarityResult(PlanDto plan, double similarity) {}

    public RecommendationResponseDto recommendByPlanSimilarity(
            List<PlanDto> targetPlans,
            PlanDto targetPlan,
            double avgDataUsage,
            UserPreferenceDto userPreference
    ) {
        log.info("요금제 유사도 기반 추천 로직 시작. 대상 요금제 ID: {}", targetPlan.getPlanId());
        try {
            double[] targetPlanVector = featureVectorGenerator.createPlanFeatureVector(targetPlan);

            List<RecommendPlanDto> recommendedPlans = targetPlans.parallelStream()
                    .map(plan -> {
                        try {
                            if (plan.getPlanId().equals(targetPlan.getPlanId())) {
                                return null;
                            }
                            log.debug("요금제 ID: {}에 대한 유사도 계산 중...", plan.getPlanId());
                            double[] planVector = featureVectorGenerator.createPlanFeatureVector(plan);

                            double similarity = similarityCalculator.calculateEuclideanSimilarity(targetPlanVector, planVector);
                            log.debug("요금제 ID: {} 최종 유사도 점수: {}", plan.getPlanId(), similarity);

                            return new PlanSimilarityResult(plan, similarity);

                        } catch (Exception e) {
                            log.error("요금제 ID: {} 처리 중 오류 발생. 이 요금제는 추천에서 제외됩니다.", plan.getPlanId(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted((a, b) -> Double.compare(b.similarity(), a.similarity()))
                    .limit(3)
                    .map(result -> RecommendPlanDto.builder()
                            .plan(result.plan())
                            .score(result.similarity())
                            .recommendationType("PLAN_SIMILARITY")
                            .build())
                    .collect(Collectors.toList());

            log.info("요금제 유사도 기반 추천 로직 완료");
            return RecommendationResponseDto.builder()
                    .userPreference(userPreference)
                    .avgDataUsage(avgDataUsage)
                    .recommendPlans(recommendedPlans)
                    .build();

        } catch (Exception e) {
            log.error("요금제 유사도 추천 로직 오류 발생", e);
            throw new RecommendationException(ErrorCode.PLAN_SIMILARITY_RECOMMENDATION_FAILURE);
        }
    }
}
