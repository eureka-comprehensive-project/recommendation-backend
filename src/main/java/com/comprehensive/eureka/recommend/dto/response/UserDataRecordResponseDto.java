package com.comprehensive.eureka.recommend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserDataRecordResponseDto {
    private Long userId;
    private Integer dataUsage;
    private String dataUsageUnit;
    private String yearMonth;
}