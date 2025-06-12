package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.WeightConstant;
import com.comprehensive.eureka.recommend.dto.RecommendationDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RecommendationCombineAndRanker {

    public List<RecommendationDto> combineAndRankRecommendations(
            List<RecommendationDto> userSimilarityResults,
            List<RecommendationDto> weightedResults,
            List<RecommendationDto> directSimilarityResults
    ) {
        Map<Integer, RecommendationDto> combinedResults = new HashMap<>();

        userSimilarityResults.forEach(result -> {
            result.setScore(result.getScore() * WeightConstant.USER_SIMILARITY_SCORE_WEIGHT);
            combinedResults.put(result.getPlan().getPlanId(), result);
        });

        weightedResults.forEach(result -> {
            Integer planId = result.getPlan().getPlanId();
            double weightedScore = result.getScore() * WeightConstant.WEIGHT_SCORE_WEIGHT;

            combinedResults.merge(planId, result, (existing, newResult) -> {
                existing.setScore(existing.getScore() + weightedScore);
                existing.setRecommendationType("HYBRID");
                return existing;
            });
        });

        directSimilarityResults.forEach(result -> {
            Integer planId = result.getPlan().getPlanId();
            double directSimilarityScore = result.getScore() * WeightConstant.USER_PLAN_SIMILARITY_WEIGHT;

            combinedResults.merge(planId, result, (existing, newResult) -> {
                existing.setScore(existing.getScore() + directSimilarityScore);
                existing.setRecommendationType("HYBRID");
                return existing;
            });
        });

        return combinedResults.values().stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(3)
                .collect(Collectors.toList());
    }
}
