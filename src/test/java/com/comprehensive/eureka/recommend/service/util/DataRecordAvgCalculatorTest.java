package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.dto.response.UserDataRecordResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DataRecordAvgCalculatorTest {

    @InjectMocks
    private DataRecordAvgCalculator dataRecordAvgCalculator;

    @Test
    @DisplayName("데이터 사용량 평균 계산 테스트 - GB 단위")
    void 데이터_사용량_평균_계산_테스트_GB_단위() {
        // given
        List<UserDataRecordResponseDto> records = Arrays.asList(
                createUserDataRecord(5, "GB"),
                createUserDataRecord(7, "GB"),
                createUserDataRecord(3, "GB")
        );

        // when
        double result = dataRecordAvgCalculator.calculateAverageDataUsage(records);

        // then
        assertEquals(5.0, result);
    }

    @Test
    @DisplayName("데이터 사용량 평균 계산 테스트 - 6개 이상의 레코드")
    void 데이터_사용량_평균_계산_테스트_6개_이상의_레코드() {
        // given
        List<UserDataRecordResponseDto> records = Arrays.asList(
                createUserDataRecord(1, "GB"),
                createUserDataRecord(2, "GB"),
                createUserDataRecord(3, "GB"),
                createUserDataRecord(4, "GB"),
                createUserDataRecord(5, "GB"),
                createUserDataRecord(6, "GB"),
                createUserDataRecord(7, "GB"),
                createUserDataRecord(8, "GB")
        );

        // when
        double result = dataRecordAvgCalculator.calculateAverageDataUsage(records);

        // then
        assertEquals(3.5, result);
    }

    @Test
    @DisplayName("데이터 사용량 평균 계산 테스트 - 빈 리스트")
    void 데이터_사용량_평균_계산_테스트_빈_리스트() {
        // given
        List<UserDataRecordResponseDto> records = Collections.emptyList();

        // when
        double result = dataRecordAvgCalculator.calculateAverageDataUsage(records);

        // then
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("데이터 사용량 평균 계산 테스트 - null 리스트")
    void 데이터_사용량_평균_계산_테스트_null_리스트() {
        // when
        double result = dataRecordAvgCalculator.calculateAverageDataUsage(null);

        // then
        assertEquals(0.0, result);
    }

    private UserDataRecordResponseDto createUserDataRecord(double dataUsage, String unit) {
        int value = (int) dataUsage;

        return UserDataRecordResponseDto.builder()
                .userId(1L)
                .dataUsage(value)
                .dataUsageUnit(unit)
                .yearMonth("202301")
                .build();
    }
}
