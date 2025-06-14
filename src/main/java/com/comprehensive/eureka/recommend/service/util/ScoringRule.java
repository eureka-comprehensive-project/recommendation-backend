package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.WeightConstant;
import com.comprehensive.eureka.recommend.dto.BenefitDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ScoringRule {

    public double calculateDataScore(Double preferredData, Double actualAvgData, Double planDataLimit) {
        double targetUsage = 0.0;
        double preferenceValue = (preferredData == null || preferredData == 0) ? 150.0 : preferredData;

        if (actualAvgData == 0.0) targetUsage = preferenceValue;
        else targetUsage = (preferenceValue * WeightConstant.DATA_PREFERENCE_WEIGHT) + (actualAvgData * WeightConstant.DATA_PATTERN_WEIGHT);

        boolean isUnlimited = (planDataLimit == 0);

        if (isUnlimited && targetUsage >= 150) return 1.0;

        double effectivePlanData = isUnlimited ? 10000.0 : planDataLimit;

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

    public double calculateBenefitScore(String userBenefit, List<BenefitDto> planBenefits) {
        if (userBenefit == null || planBenefits == null || planBenefits.isEmpty()) return 0.0;


        boolean isMatch = planBenefits.stream()
                .anyMatch(benefitDto -> userBenefit.equals(benefitDto.getBenefitName()));

        return isMatch ? 1.0 : 0.0;
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
}
