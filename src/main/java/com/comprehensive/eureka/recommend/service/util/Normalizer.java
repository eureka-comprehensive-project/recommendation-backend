package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.RangeConstant;
import org.springframework.stereotype.Component;

@Component
public class Normalizer {

    public double normalizeDataUsage(Double dataUsage) {
        if (dataUsage == null) return 0.0;
        if (dataUsage == 0) return 1.0;

        return (dataUsage - RangeConstant.MIN_PLAN_DATA) / (RangeConstant.MAX_PLAN_DATA - RangeConstant.MIN_PLAN_DATA);
    }

    public double normalizePrice(Integer price) {
        if (price == null) return 0.0;
        return (price.doubleValue() - RangeConstant.MIN_PRICE) / (RangeConstant.MAX_PRICE - RangeConstant.MIN_PRICE);
    }

    public double normalizeSharedDataUsage(Integer shareDataUsage) {
        if (shareDataUsage == null) return 0.0;
        return (shareDataUsage.doubleValue() - RangeConstant.MIN_PLAN_SHARED_DATA) / (RangeConstant.MAX_PLAN_SHARED_DATA - RangeConstant.MIN_PLAN_SHARED_DATA);
    }
}
