package com.comprehensive.eureka.recommend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackDto {
    private String keyword;
    private Long sentimentCode;
    private Long detailCode;
}
