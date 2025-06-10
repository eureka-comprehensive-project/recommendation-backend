package com.comprehensive.eureka.recommend.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDataRecordRequestDto {
    private Long userId;
    private int monthCount;
}