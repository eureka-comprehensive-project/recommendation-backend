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
public class RestUtil {

    private final WebClient webClient;

    public <R> BaseResponseDto<R> get(
            String url,
            ParameterizedTypeReference<BaseResponseDto<R>> responseType
    ) {
        Mono<BaseResponseDto<R>> responseMono = webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);

        return Objects.requireNonNull(responseMono.block());
    }

    public <R> BaseResponseDto<R> post(
            String url,
            Object requestBody,
            ParameterizedTypeReference<BaseResponseDto<R>> responseType
    ) {
        Mono<BaseResponseDto<R>> responseMono = webClient.post()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType);

        return Objects.requireNonNull(responseMono.block());
    }
}
