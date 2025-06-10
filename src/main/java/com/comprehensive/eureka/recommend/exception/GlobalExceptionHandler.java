package com.comprehensive.eureka.recommend.exception;

import com.comprehensive.eureka.recommend.dto.base.BaseResponseDto;
import com.comprehensive.eureka.recommend.dto.response.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice("com.comprehensive.eureka.recommend")
public class GlobalExceptionHandler {

    @ExceptionHandler(RecommendationException.class)
    public BaseResponseDto<ErrorResponseDto> handleRecommendationException(RecommendationException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(e.getErrorCode());
    }
}
