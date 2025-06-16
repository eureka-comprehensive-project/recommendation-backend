package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.RangeConstant;
import com.comprehensive.eureka.recommend.constant.WeightConstant;
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
public class ScoringRule {

    private final PlanApiServiceClient planApiServiceClient;

    public double calculateDataScore(Double preferredData, Double actualAvgData, Double planDataLimit) {
        double targetUsage = 0.0, preferenceValue = 0.0;

        if (preferredData == null) preferenceValue = 0.0;
        else if (preferredData == RangeConstant.UNLIMITED || preferredData > RangeConstant.MAX_PLAN_DATA) preferenceValue = RangeConstant.MAX_DATA;
        else preferenceValue = preferredData;

        if (actualAvgData == 0.0) targetUsage = preferenceValue;
        else targetUsage = (preferenceValue * WeightConstant.DATA_PREFERENCE_WEIGHT) + (actualAvgData * WeightConstant.DATA_PATTERN_WEIGHT);

        boolean isUnlimited = (planDataLimit == RangeConstant.MAX_DATA);

        if (isUnlimited && targetUsage >= 100.0) return 1.0;

        double effectivePlanData = planDataLimit;

        if (effectivePlanData < targetUsage) {
            double ratio = effectivePlanData / targetUsage;
            return Math.pow(ratio, 5.0);

        } else {
            double absurdityLimit = targetUsage * 1.5;

            if (effectivePlanData > absurdityLimit) return absurdityLimit / effectivePlanData;
            else return 1.0;
        }
    }

    public double calculatePriceScore(Integer preferredPrice, Integer planPrice) {
        double ratio = preferredPrice.doubleValue() / planPrice.doubleValue();

        if (ratio >= 1.0) return 1.0;
        return Math.max(0.1, ratio);
    }

    public double calculateSharedDataScore(Double preferredSharedData, Double planSharedData) {
        if (preferredSharedData == 0 || planSharedData == 0) return 0.0;

        double ratio = preferredSharedData/ planSharedData;

        if (ratio <= 0.8) return 1.0 - Math.abs(0.6 - ratio);
        else if (ratio <= 1.0) return 0.7;
        else return Math.max(0.1, 1.0 / ratio);
    }

    public double calculateBenefitScore(Long preferenceBenefitGroupId, PlanDto plan) {
        if (!isPlanHasBenefitGroupId(plan.getPlanId(), preferenceBenefitGroupId)) return 0.0;
        else return 1.0;
    }

    public double calculateValueAddedCallScore(Integer preferredValueAddedCallUsage, Integer planValueAddedCallAmount) {
        if (preferredValueAddedCallUsage == 0 || planValueAddedCallAmount == 0) return 0.0;

        double ratio = preferredValueAddedCallUsage.doubleValue() / planValueAddedCallAmount.doubleValue();

        if (ratio <= 0.8) return 1.0 - Math.abs(0.6 - ratio);
        else if (ratio <= 1.0) return 0.7;
        else return Math.max(0.1, 1.0 / ratio);
    }

    public double calculateFamilyDataScore(boolean isPreferredFamilyData, boolean isPlanFamilyDataEnabled) {
        if (isPreferredFamilyData && isPlanFamilyDataEnabled) return 1.0;
        else if (isPlanFamilyDataEnabled) return 0.5;
        else return 0.0;
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
