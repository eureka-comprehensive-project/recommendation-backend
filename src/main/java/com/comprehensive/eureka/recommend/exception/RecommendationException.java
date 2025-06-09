package com.comprehensive.eureka.recommend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RecommendationException extends RuntimeException{

    private final ErrorCode errorCode;
}
