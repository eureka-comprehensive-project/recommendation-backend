package com.comprehensive.eureka.recommend.dto.base;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BaseResponseDto<T> {

    private Integer statusCode;
    private String message;
    private T data;

    public static <T> BaseResponseDto<T> success(T data) {
        return BaseResponseDto.<T>builder()
                .statusCode(200)
                .message("success")
                .data(data)
                .build();
    }
}
