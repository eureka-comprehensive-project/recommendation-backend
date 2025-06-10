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

        double combinedDataUsage = pref.getPreferenceDataUsage() * WeightConstant.DATA_PREFERENCE_WEIGHT + avgDataUsage * WeightConstant.DATA_PATTERN_WEIGHT;

        return new double[]{
                normalizer.normalizeDataUsage(combinedDataUsage),
                normalizer.normalizePrice(pref.getPreferencePrice()),
                normalizer.normalizeSharedDataUsage(pref.getPreferenceSharedDataUsage())
        };
    }

    public double[] createPlanFeatureVector(PlanDto plan) {

        return new double[] {
                normalizer.normalizeDataUsage(plan.getDataAllowance().doubleValue()),
                normalizer.normalizePrice(plan.getPrice()),
                normalizer.normalizeSharedDataUsage(plan.getSharedDataAllowance()),
        };
    }
}
