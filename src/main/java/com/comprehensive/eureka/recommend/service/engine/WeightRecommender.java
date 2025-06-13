package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.UserDataRecordResponseDto;
import com.comprehensive.eureka.recommend.service.util.DataRecordAvgCalculator;
import com.comprehensive.eureka.recommend.service.util.ScoreCalculator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeightRecommender {

    private final DataRecordAvgCalculator dataRecordAvgCalculator;
    private final ScoreCalculator scoreCalculator;

    public List<RecommendPlanDto> recommendByWeightScore(
            UserPreferenceDto targetUserPreference,
            List<PlanDto> targetPlans,
            List<UserDataRecordResponseDto> targetUserHistory
    ) {
        double avgDataUsage = dataRecordAvgCalculator.calculateAverageDataUsage(targetUserHistory);

        return targetPlans.stream()
                .map(plan ->{
                    double score = scoreCalculator.calculateWeightedScore(targetUserPreference, avgDataUsage, plan);
                    log.info("요금제 ID: {}에 대한 가중치 점수: {}", plan.getPlanId(), score);

                    return RecommendPlanDto.builder()
                            .plan(plan)
                            .score(score)
                            .recommendationType("WEIGHTED")
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(5)
                .toList();
    }
}
