package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.RangeConstant;
import com.comprehensive.eureka.recommend.entity.enums.DataPeriod;
import org.springframework.stereotype.Component;

@Component
public class UnitConverter {

    public static Double convertToGigabytes(Integer value, String unit) {
        if (value == null) return 0.0;
        if (value == RangeConstant.UNLIMITED) return RangeConstant.MAX_DATA;

        return (double) switch (unit) {
            case "MB" -> value / 1024.0;
            case "GB" -> (double) value;
            case "TB" -> value * 1024.0;
            default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
        };
    }

    public static Double convertToGigabytes(Integer value, String unit, DataPeriod dataPeriod) {
        if (value == null) return 0.0;
        if (value == RangeConstant.UNLIMITED) return RangeConstant.MAX_DATA;
        if (dataPeriod == DataPeriod.DAY) value *= 30;

        return (double) switch (unit) {
            case "MB" -> value / 1024.0;
            case "GB" -> (double) value;
            case "TB" -> value * 1024.0;
            default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
        };
    }
}
