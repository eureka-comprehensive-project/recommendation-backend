package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.dto.response.UserDataRecordResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataRecordAvgCalculator {

    public double calculateAverageDataUsage(List<UserDataRecordResponseDto> records) {
        if (records == null || records.isEmpty()) return 0.0;

        return records.stream()
                .limit(6)
                .mapToDouble(record -> {
                    int dataUsage = record.getDataUsage();
                    String unit = record.getDataUsageUnit();
                    return UnitConverter.convertToGigabytes(dataUsage, unit);
                })
                .average()
                .orElse(0.0);
    }
}
