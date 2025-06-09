package com.comprehensive.eureka.recommend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // API 호출 관련 에러 코드 (30000 ~ 30010)
    USER_PREFERENCE_LOAD_FAILURE(30000, "USER_PREFERENCE_NOT_FOUND", "사용자 선호 정보 호출에 실패했습니다."),
    PLAN_LOAD_FAILURE(30001, "PLAN_LOAD_FAILURE", "요금제 호출에 실패했습니다."),
    USER_PLAN_RECORD_LOAD_FAILURE(30002, "USER_PLAN_RECORD_LOAD_FAILURE", "고객 요금제 기록 호출에 실패했습니다."),
    USER_DATA_RECORD_LOAD_FAILURE(30003, "USER_DATA_RECORD_LOAD_FAILURE", "고객 데이터 기록 호출에 실패했습니다."),
    USER_LOAD_FAILURE(30004, "USER_LOAD_FAILURE", "고객 정보 호출에 실패했습니다."),

    // 추천 로직 관련 에러 코드 (30010 ~ 30020)
    BASIC_RECOMMENDATION_FAILURE(30010, "BASIC_RECOMMENDATION_FAILURE", "기본 추천 로직에 실패했습니다."),
    USER_SIMILAR_RECOMMENDATION_FAILURE(30011, "USER_SIMILARITY_CALCULATION_FAILURE", "사용자 유사도 기반 추천 로직에 실패했습니다."),
    USER_PLAN_SIMILAR_RECOMMENDATION_FAILURE(30012, "USER_PLAN_SIMILARITY_CALCULATION_FAILURE", "사용자 요금제 유사도 기반 추천 로직에 실패했습니다."),
    WEIGHTED_PLAN_RECOMMENDATION_FAILURE(30013, "WEIGHTED_PLAN_RECOMMENDATION_FAILURE", "가중치 기반 요금제 추천 로직에 실패했습니다."),

    // 추천 로직 계산 메서드 관련 에러 코드 (30020 ~ 30030)
    FEATURE_VECTOR_GENERATION_FAILURE(30020, "FEATURE_VECTOR_GENERATION_FAILURE", "특성 벡터 생성에 실패했습니다."),
    DATA_AVERAGE_CALCULATION_FAILURE(30021, "DATA_AVERAGE_CALCULATION_FAILURE", "사용자 데이터 기록 평균 계산에 실패했습니다."),
    NORMALIZATION_FAILURE(30022, "NORMALIZATION_FAILURE", "데이터 정규화에 실패했습니다."),
    PLAN_FILTER_FAILURE(30023, "PLAN_FILTER_FAILURE", "요금제 필터링에 실패했습니다."),
    WEIGHTED_SCORE_CALCULATION_FAILURE(30024, "WEIGHTED_SCORE_CALCULATION_FAILURE", "가중치 점수 계산에 실패했습니다."),
    SIMILARITY_CALCULATION_FAILURE(30025, "SIMILARITY_CALCULATION_FAILURE", "유사도 계산에 실패했습니다."),
    UnIT_CONVERSION_FAILURE(30026, "UTILITY_CONVERSION_FAILURE", "데이터 단위 변환에 실패했습니다.");


    private final int code;
    private final String name;
    private final String message;
}
