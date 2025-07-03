package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimilarityCalculatorTest {

    @Mock
    private PlanApiServiceClient planApiServiceClient;

    @InjectMocks
    private SimilarityCalculator similarityCalculator;

    @Test
    @DisplayName("코사인 유사도 계산 테스트 - 동일한 벡터")
    void 코사인_유사도_계산_테스트_동일한_벡터() {
        // given
        double[] vector1 = {0.5, 0.7, 0.3};
        double[] vector2 = {0.5, 0.7, 0.3};

        // when
        double similarity = similarityCalculator.calculateCosineSimilarity(vector1, vector2);

        // then
        assertEquals(0.9999999999999999, similarity);
    }

    @Test
    @DisplayName("코사인 유사도 계산 테스트 - 다른 벡터")
    void 코사인_유사도_계산_테스트_다른_벡터() {
        // given
        double[] vector1 = {0.5, 0.7, 0.3};
        double[] vector2 = {0.2, 0.9, 0.1};

        // when
        double similarity = similarityCalculator.calculateCosineSimilarity(vector1, vector2);

        // then
        assertTrue(similarity < 1.0);
        assertTrue(similarity > 0.0);
    }

    @Test
    @DisplayName("코사인 유사도 계산 테스트 - 영벡터")
    void 코사인_유사도_계산_테스트_영벡터() {
        // given
        double[] vector1 = {0.0, 0.0, 0.0};
        double[] vector2 = {0.5, 0.7, 0.3};

        // when
        double similarity = similarityCalculator.calculateCosineSimilarity(vector1, vector2);

        // then
        assertEquals(0.0, similarity);
    }

    @Test
    @DisplayName("유클리드 유사도 계산 테스트 - 동일한 벡터")
    void 유클리드_유사도_계산_테스트_동일한_벡터() {
        // given
        double[] vector1 = {0.5, 0.7, 0.3};
        double[] vector2 = {0.5, 0.7, 0.3};

        // when
        double similarity = similarityCalculator.calculateEuclideanSimilarity(vector1, vector2);

        // then
        assertEquals(1.0, similarity);
    }

    @Test
    @DisplayName("유클리드 유사도 계산 테스트 - 다른 벡터")
    void 유클리드_유사도_계산_테스트_다른_벡터() {
        // given
        double[] vector1 = {0.5, 0.7, 0.3};
        double[] vector2 = {0.2, 0.9, 0.1};

        // when
        double similarity = similarityCalculator.calculateEuclideanSimilarity(vector1, vector2);

        // then
        assertTrue(similarity < 1.0);
        assertTrue(similarity > 0.0);
    }

    @Test
    @DisplayName("사용자-요금제 혜택 유사도 계산 테스트 - 요금제가 선호 혜택 그룹 ID를 가진 경우")
    void 사용자_요금제_혜택_유사도_계산_테스트_요금제가_선호_혜택_그룹_ID를_가진_경우() {
        // given
        Long preferenceBenefitGroupId = 1L;
        PlanDto plan = PlanDto.builder()
                .planId(1)
                .build();

        when(planApiServiceClient.isPlanHasBenefitGroupId(anyInt(), anyLong())).thenReturn(true);

        // when
        double similarity = similarityCalculator.calculateUserPlanBenefitSimilarity(preferenceBenefitGroupId, plan);

        // then
        assertEquals(1.0, similarity);
    }

    @Test
    @DisplayName("사용자-요금제 혜택 유사도 계산 테스트 - 요금제가 선호 혜택 그룹 ID를 가지지 않은 경우")
    void 사용자_요금제_혜택_유사도_계산_테스트_요금제가_선호_혜택_그룹_ID를_가지지_않은_경우() {
        // given
        Long preferenceBenefitGroupId = 1L;
        PlanDto plan = PlanDto.builder()
                .planId(1)
                .build();

        when(planApiServiceClient.isPlanHasBenefitGroupId(anyInt(), anyLong())).thenReturn(false);

        // when
        double similarity = similarityCalculator.calculateUserPlanBenefitSimilarity(preferenceBenefitGroupId, plan);

        // then
        assertEquals(0.0, similarity);
    }

    @Test
    @DisplayName("사용자-요금제 혜택 유사도 계산 테스트 - 선호 혜택 그룹 ID가 null인 경우")
    void 사용자_요금제_혜택_유사도_계산_테스트_선호_혜택_그룹_ID가_null인_경우() {
        // given
        Long preferenceBenefitGroupId = null;
        PlanDto plan = PlanDto.builder()
                .planId(1)
                .build();

        // when
        double similarity = similarityCalculator.calculateUserPlanBenefitSimilarity(preferenceBenefitGroupId, plan);

        // then
        assertEquals(0.0, similarity);
    }
}