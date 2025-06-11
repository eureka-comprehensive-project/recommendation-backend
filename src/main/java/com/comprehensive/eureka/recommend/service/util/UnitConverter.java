package com.comprehensive.eureka.recommend.service.util;

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
}
