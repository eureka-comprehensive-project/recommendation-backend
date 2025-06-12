package com.comprehensive.eureka.recommend.util;

import com.comprehensive.eureka.recommend.dto.base.BaseResponseDto;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class WebClientUtil {

    private final WebClient webClient;

    public <R> BaseResponseDto<R> get(
            String url,
            ParameterizedTypeReference<BaseResponseDto<R>> responseType
    ) {
        return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    public <T, R> BaseResponseDto<R> post(String url, T requestBody, ParameterizedTypeReference<BaseResponseDto<R>> responseType) {
        return webClient.post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }
}
