package com.comprehensive.eureka.recommend.util.api;

import com.comprehensive.eureka.recommend.constant.DomainConstant;
import com.comprehensive.eureka.recommend.dto.base.BaseResponseDto;
import com.comprehensive.eureka.recommend.util.RestUtil;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserApiServiceClient {

    private final RestUtil restUtil;

    public LocalDate getUserBirthDay(Long userId) {
        BaseResponseDto<LocalDate> response = restUtil.get(
                DomainConstant.USER_DOMAIN + userId + "/birthday",
                new ParameterizedTypeReference<BaseResponseDto<LocalDate>>() {}
        );

        return response.getData();
    }
}
