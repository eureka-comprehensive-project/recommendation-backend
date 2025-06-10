package com.comprehensive.eureka.recommend.util.api;

import com.comprehensive.eureka.recommend.constant.DomainConstant;
import com.comprehensive.eureka.recommend.dto.base.BaseResponseDto;
import com.comprehensive.eureka.recommend.dto.request.UserDataRecordRequestDto;
import com.comprehensive.eureka.recommend.dto.response.UserDataRecordResponseDto;
import com.comprehensive.eureka.recommend.util.RestUtil;
import java.time.LocalDate;
import java.util.List;
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

    public List<UserDataRecordResponseDto> getUserDataRecords(Long userId) {
        UserDataRecordRequestDto requestDto = UserDataRecordRequestDto.builder()
                .userId(userId)
                .monthCount(6)
                .build();

        BaseResponseDto<List<UserDataRecordResponseDto>> response = restUtil.post(
                DomainConstant.BASE_DOMAIN + "user-data-record/usage",
                requestDto,
                new ParameterizedTypeReference<BaseResponseDto<List<UserDataRecordResponseDto>>>() {}
        );

        return response.getData();
    }
}
