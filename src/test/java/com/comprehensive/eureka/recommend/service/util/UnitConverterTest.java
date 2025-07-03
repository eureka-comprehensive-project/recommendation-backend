package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.RangeConstant;
import com.comprehensive.eureka.recommend.entity.enums.DataPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitConverterTest {

    @Test
    @DisplayName("MB를 GB로 변환 테스트")
    void MB를_GB로_변환_테스트() {
        // given
        Integer value = 1024;
        String unit = "MB";

        // when
        Double result = UnitConverter.convertToGigabytes(value, unit);

        // then
        assertEquals(1.0, result);
    }

    @Test
    @DisplayName("GB를 GB로 변환 테스트")
    void GB를_GB로_변환_테스트() {
        // given
        Integer value = 5;
        String unit = "GB";

        // when
        Double result = UnitConverter.convertToGigabytes(value, unit);

        // then
        assertEquals(5.0, result);
    }

    @Test
    @DisplayName("TB를 GB로 변환 테스트")
    void TB를_GB로_변환_테스트() {
        // given
        Integer value = 1;
        String unit = "TB";

        // when
        Double result = UnitConverter.convertToGigabytes(value, unit);

        // then
        assertEquals(1024.0, result);
    }

    @Test
    @DisplayName("null 값 변환 테스트")
    void null_값_변환_테스트() {
        // given
        Integer value = null;
        String unit = "GB";

        // when
        Double result = UnitConverter.convertToGigabytes(value, unit);

        // then
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("무제한 데이터 변환 테스트")
    void 무제한_데이터_변환_테스트() {
        // given
        Integer value = RangeConstant.UNLIMITED;
        String unit = "GB";

        // when
        Double result = UnitConverter.convertToGigabytes(value, unit);

        // then
        assertEquals(RangeConstant.MAX_DATA, result);
    }

    @Test
    @DisplayName("지원되지 않는 단위 변환 테스트")
    void 지원되지_않는_단위_변환_테스트() {
        // given
        Integer value = 10;
        String unit = "KB";

        // then
        assertThrows(IllegalArgumentException.class, () -> {
            UnitConverter.convertToGigabytes(value, unit);
        });
    }

    @Test
    @DisplayName("일일 데이터를 월간 GB로 변환 테스트")
    void 일일_데이터를_월간_GB로_변환_테스트() {
        // given
        Integer value = 1;
        String unit = "GB";
        DataPeriod dataPeriod = DataPeriod.DAY;

        // when
        Double result = UnitConverter.convertToGigabytes(value, unit, dataPeriod);

        // then
        assertEquals(30.0, result);
    }

    @Test
    @DisplayName("월간 데이터를 월간 GB로 변환 테스트")
    void 월간_데이터를_월간_GB로_변환_테스트() {
        // given
        Integer value = 10;
        String unit = "GB";
        DataPeriod dataPeriod = DataPeriod.MONTH;

        // when
        Double result = UnitConverter.convertToGigabytes(value, unit, dataPeriod);

        // then
        assertEquals(10.0, result);
    }
}