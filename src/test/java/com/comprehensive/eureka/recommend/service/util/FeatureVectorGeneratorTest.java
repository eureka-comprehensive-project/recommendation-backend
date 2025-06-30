package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.WeightConstant;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.entity.enums.DataPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureVectorGeneratorTest {

    @Mock
    private Normalizer normalizer;

    @InjectMocks
    private FeatureVectorGenerator featureVectorGenerator;

    @Test
    @DisplayName("사용자 특성 벡터 생성 테스트 - 평균 데이터 사용량이 0인 경우")
    void 사용자_특성_벡터_생성_테스트_평균_데이터_사용량이_0인_경우() {
        // given
        UserPreferenceDto userPreference = UserPreferenceDto.builder()
                .preferenceDataUsage(10)
                .preferenceDataUsageUnit("GB")
                .preferencePrice(50000)
                .preferenceSharedDataUsage(5)
                .preferenceSharedDataUsageUnit("GB")
                .build();
        
        double avgDataUsage = 0.0;
        
        // 정규화된 값 설정
        when(normalizer.normalizeDataUsage(10.0)).thenReturn(0.5);
        when(normalizer.normalizePrice(50000)).thenReturn(0.6);
        when(normalizer.normalizeSharedDataUsage(5)).thenReturn(0.7);
        
        // when
        double[] result = featureVectorGenerator.createUserFeatureVector(userPreference, avgDataUsage);
        
        // then
        assertEquals(3, result.length);
        assertEquals(0.5, result[0]); // 데이터 사용량
        assertEquals(0.6, result[1]); // 가격
        assertEquals(0.7, result[2]); // 공유 데이터
    }
    
    @Test
    @DisplayName("사용자 특성 벡터 생성 테스트 - 평균 데이터 사용량이 있는 경우")
    void 사용자_특성_벡터_생성_테스트_평균_데이터_사용량이_있는_경우() {
        // given
        UserPreferenceDto userPreference = UserPreferenceDto.builder()
                .preferenceDataUsage(10)
                .preferenceDataUsageUnit("GB")
                .preferencePrice(50000)
                .preferenceSharedDataUsage(5)
                .preferenceSharedDataUsageUnit("GB")
                .build();
        
        double avgDataUsage = 15.0;

        double targetUsage = (10 * WeightConstant.DATA_PREFERENCE_WEIGHT) + (15 * WeightConstant.DATA_PATTERN_WEIGHT);
        
        // 정규화된 값 설정
        when(normalizer.normalizeDataUsage(targetUsage)).thenReturn(0.55);
        when(normalizer.normalizePrice(50000)).thenReturn(0.6);
        when(normalizer.normalizeSharedDataUsage(5)).thenReturn(0.7);
        
        // when
        double[] result = featureVectorGenerator.createUserFeatureVector(userPreference, avgDataUsage);
        
        // then
        assertEquals(3, result.length);
        assertEquals(0.55, result[0]); // 데이터 사용량
        assertEquals(0.6, result[1]); // 가격
        assertEquals(0.7, result[2]); // 공유 데이터
    }
    
    @Test
    @DisplayName("요금제 특성 벡터 생성 테스트")
    void 요금제_특성_벡터_생성_테스트() {
        // given
        PlanDto plan = PlanDto.builder()
                .dataAllowance(20)
                .dataAllowanceUnit("GB")
                .dataPeriod(DataPeriod.MONTH)
                .monthlyFee(60000)
                .tetheringDataAmount(10)
                .tetheringDataUnit("GB")
                .build();
        
        // 정규화된 값 설정
        when(normalizer.normalizeDataUsage(20.0)).thenReturn(0.8);
        when(normalizer.normalizePrice(60000)).thenReturn(0.7);
        when(normalizer.normalizeSharedDataUsage(10)).thenReturn(0.9);
        
        // when
        double[] result = featureVectorGenerator.createPlanFeatureVector(plan);
        
        // then
        assertEquals(3, result.length);
        assertEquals(0.8, result[0]); // 데이터 사용량
        assertEquals(0.7, result[1]); // 가격
        assertEquals(0.9, result[2]); // 공유 데이터
    }
    
    @Test
    @DisplayName("요금제 특성 벡터 생성 테스트 - 일일 데이터 요금제")
    void 요금제_특성_벡터_생성_테스트_일일_데이터_요금제() {
        // given
        PlanDto plan = PlanDto.builder()
                .dataAllowance(1)
                .dataAllowanceUnit("GB")
                .dataPeriod(DataPeriod.DAY)
                .monthlyFee(45000)
                .tetheringDataAmount(5)
                .tetheringDataUnit("GB")
                .build();
        
        // 일일 1GB는 월 30GB로 변환됨
        when(normalizer.normalizeDataUsage(30.0)).thenReturn(0.85);
        when(normalizer.normalizePrice(45000)).thenReturn(0.65);
        when(normalizer.normalizeSharedDataUsage(5)).thenReturn(0.75);
        
        // when
        double[] result = featureVectorGenerator.createPlanFeatureVector(plan);
        
        // then
        assertEquals(3, result.length);
        assertEquals(0.85, result[0]); // 데이터 사용량
        assertEquals(0.65, result[1]); // 가격
        assertEquals(0.75, result[2]); // 공유 데이터
    }
}