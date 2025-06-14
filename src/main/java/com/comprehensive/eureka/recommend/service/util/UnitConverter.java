package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.entity.enums.DataPeriod;
import org.springframework.stereotype.Component;

@Component
public class UnitConverter {

    public static Integer convertToGigabytes(Integer value, String unit) {
        return switch (unit) {
            case "MB" -> value / 1024;
            case "GB" -> value;
            case "TB" -> value * 1024;
            default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
        };
    }

    public static Integer convertToGigabytes(Integer value, String unit, DataPeriod dataPeriod) {
        if (dataPeriod == DataPeriod.DAY) value *= 30;

        return switch (unit) {
            case "MB" -> value / 1024;
            case "GB" -> value;
            case "TB" -> value * 1024;
            default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
        };
    }
}
