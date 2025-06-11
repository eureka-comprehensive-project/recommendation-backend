package com.comprehensive.eureka.recommend.util;

import com.comprehensive.eureka.recommend.dto.base.BaseResponseDto;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class RestUtil {

    private final RestTemplate restTemplate;

    public <R> BaseResponseDto<R> get(
            String url,
            ParameterizedTypeReference<BaseResponseDto<R>> responseType
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<BaseResponseDto<R>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                responseType
        );

        return Objects.requireNonNull(response.getBody());
    }

    public <R> BaseResponseDto<R> post(
            String url,
            Object requestBody,
            ParameterizedTypeReference<BaseResponseDto<R>> responseType
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<BaseResponseDto<R>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                responseType
        );

        return Objects.requireNonNull(response.getBody());
    }
}
