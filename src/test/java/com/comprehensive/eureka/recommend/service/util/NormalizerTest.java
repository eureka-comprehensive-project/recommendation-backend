package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.RangeConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NormalizerTest {

    @InjectMocks
    private Normalizer normalizer;

    @Test
    @DisplayName("데이터 사용량 정규화 테스트 - 최소값")
    void 데이터_사용량_정규화_테스트_최소값() {
        // given
        Double dataUsage = RangeConstant.MIN_PLAN_DATA;

        // when
        double result = normalizer.normalizeDataUsage(dataUsage);

        // then
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("데이터 사용량 정규화 테스트 - 최대값")
    void 데이터_사용량_정규화_테스트_최대값() {
        // given
        Double dataUsage = RangeConstant.MAX_PLAN_DATA;

        // when
        double result = normalizer.normalizeDataUsage(dataUsage);

        // then
        assertEquals(1.0, result);
    }

    @Test
    @DisplayName("데이터 사용량 정규화 테스트 - null 값")
    void 데이터_사용량_정규화_테스트_null_값() {
        // given
        Double dataUsage = null;

        // when
        double result = normalizer.normalizeDataUsage(dataUsage);

        // then
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("가격 정규화 테스트 - 최소값")
    void 가격_정규화_테스트_최소값() {
        // given
        Integer price = (int) RangeConstant.MIN_PRICE;

        // when
        double result = normalizer.normalizePrice(price);

        // then
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("가격 정규화 테스트 - 최대값")
    void 가격_정규화_테스트_최대값() {
        // given
        Integer price = (int) RangeConstant.MAX_PRICE;

        // when
        double result = normalizer.normalizePrice(price);

        // then
        assertEquals(1.0, result);
    }

    @Test
    @DisplayName("가격 정규화 테스트 - 중간값")
    void 가격_정규화_테스트_중간값() {
        // given
        Integer price = (int) ((RangeConstant.MIN_PRICE + RangeConstant.MAX_PRICE) / 2);

        // when
        double result = normalizer.normalizePrice(price);

        // then
        assertEquals(0.5, result);
    }

    @Test
    @DisplayName("가격 정규화 테스트 - null 값")
    void 가격_정규화_테스트_null_값() {
        // given
        Integer price = null;

        // when
        double result = normalizer.normalizePrice(price);

        // then
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("공유 데이터 사용량 정규화 테스트 - 최소값")
    void 공유_데이터_사용량_정규화_테스트_최소값() {
        // given
        Integer sharedDataUsage = (int) RangeConstant.MIN_PLAN_SHARED_DATA;

        // when
        double result = normalizer.normalizeSharedDataUsage(sharedDataUsage);

        // then
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("공유 데이터 사용량 정규화 테스트 - 최대값")
    void 공유_데이터_사용량_정규화_테스트_최대값() {
        // given
        Integer sharedDataUsage = (int) RangeConstant.MAX_PLAN_SHARED_DATA;

        // when
        double result = normalizer.normalizeSharedDataUsage(sharedDataUsage);

        // then
        assertEquals(1.0, result);
    }

    @Test
    @DisplayName("공유 데이터 사용량 정규화 테스트 - 중간값")
    void 공유_데이터_사용량_정규화_테스트_중간값() {
        // given
        Integer sharedDataUsage = (int) ((RangeConstant.MIN_PLAN_SHARED_DATA + RangeConstant.MAX_PLAN_SHARED_DATA) / 2);

        // when
        double result = normalizer.normalizeSharedDataUsage(sharedDataUsage);

        // then
        assertEquals(0.5, result);
    }

    @Test
    @DisplayName("공유 데이터 사용량 정규화 테스트 - null 값")
    void 공유_데이터_사용량_정규화_테스트_null_값() {
        // given
        Integer sharedDataUsage = null;

        // when
        double result = normalizer.normalizeSharedDataUsage(sharedDataUsage);

        // then
        assertEquals(0.0, result);
    }
}
