package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.WeightConstant;
import com.comprehensive.eureka.recommend.dto.RecommendationDto;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecommendationCombineAndRanker {

    public List<RecommendationDto> combineAndRankRecommendations(
            List<RecommendationDto> userSimilarityResults,
            List<RecommendationDto> weightedResults,
            List<RecommendationDto> directSimilarityResults
    ) {
        log.info("추천 결과 조합 및 최종 추천 결과 생성 시작");

        try {
            Map<Integer, RecommendationDto> combinedResults = new HashMap<>();

            userSimilarityResults.forEach(result -> {
                result.setScore(result.getScore() * WeightConstant.USER_SIMILARITY_SCORE_WEIGHT);
                combinedResults.put(result.getPlan().getPlanId(), result);
            });
            log.debug("사용자 유사도 기반 추천 결과 처리");

            weightedResults.forEach(result -> {
                Integer planId = result.getPlan().getPlanId();
                double weightedScore = result.getScore() * WeightConstant.WEIGHT_SCORE_WEIGHT;

                combinedResults.merge(planId, result, (existing, newResult) -> {
                    existing.setScore(existing.getScore() + weightedScore);
                    existing.setRecommendationType("HYBRID");
                    return existing;
                });
            });
            log.debug("가중치 기반 추천 결과 처리");

            directSimilarityResults.forEach(result -> {
                Integer planId = result.getPlan().getPlanId();
                double directSimilarityScore = result.getScore() * WeightConstant.USER_PLAN_SIMILARITY_WEIGHT;

                combinedResults.merge(planId, result, (existing, newResult) -> {
                    existing.setScore(existing.getScore() + directSimilarityScore);
                    existing.setRecommendationType("HYBRID");
                    return existing;
                });
            });
            log.debug("사용자 - 요금제 간 유사도 기반 추천 결과 처리");

            List<RecommendationDto> finalRecommendations = combinedResults.values().stream()
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(3)
                    .collect(Collectors.toList());

            log.info("추천 결과 조합 및 최종 결과 생성 완료");
            return finalRecommendations;

        } catch (Exception e) {
            log.error("추천 결과 조합 및 최종 결과 생성 중 오류가 발생했습니다.", e);
            throw new RecommendationException(ErrorCode.COMBINE_AND_RANK_RECOMMENDATION_FAILURE);
        }
    }
}