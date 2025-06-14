package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityCalculator {

    private final PlanApiServiceClient planApiServiceClient;

    public double calculateCosineSimilarity(double[] vector1, double[] vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }

        if (norm1 == 0.0 || norm2 == 0.0) return 0.0;

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public double calculateEuclideanSimilarity(double[] vector1, double[] vector2) {
        double distance = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            distance += Math.pow(vector1[i] - vector2[i], 2);
        }
        distance = Math.sqrt(distance);
        return 1.0 / (1.0 + distance);
    }

    public double calculateUserPlanBenefitSimilarity(Long preferenceBenefitGroupId, PlanDto plan) {
        if (!isPlanHasBenefitGroupId(plan.getPlanId(), preferenceBenefitGroupId)) return 0.0;
        else return 1.0;
    }

    private Boolean isPlanHasBenefitGroupId(Integer planId, Long benefitGroupId) {
        try {
            return planApiServiceClient.isPlanHasBenefitGroupId(planId, benefitGroupId);

        } catch (Exception e) {
            log.error("[외부 API 호출 실패] planId: {} 의 혜택 그룹 ID: {} 존재 여부 확인에 실패했습니다.", planId, benefitGroupId, e);
            throw new RecommendationException(ErrorCode.PLAN_BENEFIT_GROUP_ID_CHECK_FAILURE);
        }
    }
}
