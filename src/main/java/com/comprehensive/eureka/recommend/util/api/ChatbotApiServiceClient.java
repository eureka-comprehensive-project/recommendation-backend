package com.comprehensive.eureka.recommend.util.api;

import com.comprehensive.eureka.recommend.constant.DomainConstant;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.base.BaseResponseDto;
import com.comprehensive.eureka.recommend.util.RestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatbotApiServiceClient {

    private final RestUtil restUtil;

    public UserPreferenceDto getUserPreference(Long userId) {
        BaseResponseDto<UserPreferenceDto> response = restUtil.get(
                DomainConstant.CHATBOT_DOMAIN + "preference/" + userId,
                new ParameterizedTypeReference<BaseResponseDto<UserPreferenceDto>>() {}
        );

        return response.getData();
    }
}
