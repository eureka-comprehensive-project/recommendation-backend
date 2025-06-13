package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.constant.WeightConstant;
import com.comprehensive.eureka.recommend.dto.BenefitDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.UserDataRecordResponseDto;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.service.util.DataRecordAvgCalculator;
import com.comprehensive.eureka.recommend.service.util.FeatureVectorGenerator;
import com.comprehensive.eureka.recommend.service.util.ScoreCalculator;
import com.comprehensive.eureka.recommend.service.util.SimilarityCalculator;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPlanSimilarRecommender {

    private final PlanApiServiceClient planApiServiceClient;

    private final DataRecordAvgCalculator dataRecordAvgCalculator;
    private final FeatureVectorGenerator featureVectorGenerator;
    private final SimilarityCalculator similarityCalculator;
    private final ScoreCalculator scoreCalculator;

    private record UserPlanSimilarityResult(PlanDto plan, double similarity) {}

    public List<RecommendPlanDto> recommendByUserPlanSimilarity(
            UserPreferenceDto targetUserPreference,
            List<UserDataRecordResponseDto> targetUserHistory,
            List<PlanDto> targetPlans
    ){
        log.info("사용자-요금제 유사도 기반 추천 로직 시작. 대상 사용자 ID: {}, 대상 요금제 개수: {}", targetUserPreference.getUserId(), targetPlans.size());
        try {
            double targetAvgDataUsage = dataRecordAvgCalculator.calculateAverageDataUsage(targetUserHistory);
            double[] targetUserVector = featureVectorGenerator.createUserFeatureVector(targetUserPreference, targetAvgDataUsage);

            List<RecommendPlanDto> recommendations = targetPlans.parallelStream()
                    .map(plan -> {
                        try {
                            log.info("요금제 ID: {}에 대한 유사도 계산 중...", plan.getPlanId());
                            List<BenefitDto> targetPlanBenefits = fetchPlanBenefits(plan.getPlanId());
                            double[] planVector = featureVectorGenerator.createPlanFeatureVector(plan);

                            double numericSimilarity = similarityCalculator.calculateCosineSimilarity(targetUserVector, planVector);
                            double benefitSimilarity = similarityCalculator.calculateUserPlanBenefitSimilarity(targetUserPreference.getPreferenceBenefit(), targetPlanBenefits);
                            double sufficiencyScore = scoreCalculator.calculateSufficiencyScore(plan, targetUserPreference);

                            double baseSimilarity = (numericSimilarity * WeightConstant.NUMERIC_SIMILARITY_WEIGHT) + (benefitSimilarity * WeightConstant.BENEFIT_SIMILARITY_WEIGHT);
                            double finalSimilarity = baseSimilarity * sufficiencyScore;
                            log.info("요금제 ID: {} 최종 유사도 점수: {}", plan.getPlanId(), finalSimilarity);

                            return new UserPlanSimilarityResult(plan, finalSimilarity);

                        } catch (Exception e) {
                            log.error("요금제 ID: {} 처리 중 오류 발생. 이 요금제는 추천에서 제외됩니다.", plan.getPlanId(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted((a, b) -> Double.compare(b.similarity(), a.similarity()))
                    .limit(5)
                    .map(result -> RecommendPlanDto.builder()
                            .plan(result.plan())
                            .score(result.similarity())
                            .recommendationType("USER_PLAN_SIMILARITY")
                            .build())
                    .collect(Collectors.toList());

            log.info("사용자-요금제 유사도 기반 추천 로직 완료");
            return recommendations;

        } catch (Exception e) {
            log.error("사용자-요금제 유사도 추천 프로세스 오류 발생. 사용자 ID: {}", targetUserPreference.getUserId(), e);
            throw new RecommendationException(ErrorCode.USER_PLAN_SIMILAR_RECOMMENDATION_FAILURE);
        }
    }

    private List<BenefitDto> fetchPlanBenefits(Integer planId) {
        try {
            return planApiServiceClient.getBenefitsByPlanId(planId);

        } catch (Exception e) {
            log.error("[외부 API 호출 실패] planId: {} 의 혜택 정보 호출에 실패했습니다.", planId, e);
            throw new RecommendationException(ErrorCode.PLAN_BENEFIT_LOAD_FAILURE);
        }
    }
}