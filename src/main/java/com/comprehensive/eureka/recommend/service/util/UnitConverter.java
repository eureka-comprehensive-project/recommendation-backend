package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.entity.enums.DataPeriod;
import org.springframework.stereotype.Component;

@Component
public class UnitConverter {

    public static Double convertToGigabytes(Integer value, String unit) {
        return (double) switch (unit) {
            case "MB" -> value / 1024.0;
            case "GB" -> (double) value;
            case "TB" -> value * 1024.0;
            default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
        };
    }

    public static Double convertToGigabytes(Integer value, String unit, DataPeriod dataPeriod) {
        if (dataPeriod == DataPeriod.DAY) value *= 30;

        return (double) switch (unit) {
            case "MB" -> value / 1024.0;
            case "GB" -> (double) value;
            case "TB" -> value * 1024.0;
            default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
        };
    }
}
