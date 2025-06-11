package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.WeightConstant;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureVectorGenerator {

    private final Normalizer normalizer;

    public double[] createUserFeatureVector(UserPreferenceDto pref, double avgDataUsage) {

        double preferenceDataUsage = UnitConverter.convertToGigabytes(pref.getPreferenceDataUsage(), pref.getPreferenceDataUsageUnit());

        double combinedDataUsage = preferenceDataUsage * WeightConstant.DATA_PREFERENCE_WEIGHT + avgDataUsage * WeightConstant.DATA_PATTERN_WEIGHT;

        return new double[]{
                normalizer.normalizeDataUsage(combinedDataUsage),
                normalizer.normalizePrice(pref.getPreferencePrice()),
                normalizer.normalizeSharedDataUsage(pref.getPreferenceSharedDataUsage())
        };
    }

    public double[] createPlanFeatureVector(PlanDto plan) {

        double planDataAllowance = UnitConverter.convertToGigabytes(plan.getDataAllowance(), plan.getDataAllowanceUnit());

        return new double[] {
                normalizer.normalizeDataUsage(planDataAllowance),
                normalizer.normalizePrice(plan.getPrice()),
                normalizer.normalizeSharedDataUsage(plan.getSharedDataAllowance()),
        };
    }
}
