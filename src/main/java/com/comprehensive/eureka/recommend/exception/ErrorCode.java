package com.comprehensive.eureka.recommend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    USER_PREFERENCE_LOAD_FAILURE(30000, "USER_PREFERENCE_NOT_FOUND", "사용자 선호 정보 호출에 실패했습니다."),
    PLAN_LOAD_FAILURE(30001, "PLAN_LOAD_FAILURE", "요금제 호출에 실패했습니다."),
    USER_PLAN_RECORD_LOAD_FAILURE(30002, "USER_PLAN_RECORD_LOAD_FAILURE", "고객 요금제 기록 호출에 실패했습니다."),
    USER_DATA_RECORD_LOAD_FAILURE(30003, "USER_DATA_RECORD_LOAD_FAILURE", "고객 데이터 기록 호출에 실패했습니다."),
    USER_LOAD_FAILURE(30004, "USER_LOAD_FAILURE", "고객 정보 호출에 실패했습니다."),
    PLAN_BENEFIT_LOAD_FAILURE(30005, "PLAN_BENEFIT_LOAD_FAILURE", "요금제 혜택 목록 호출에 실패했습니다."),
    PLAN_BENEFIT_GROUP_ID_CHECK_FAILURE(30006, "PLAN_BENEFIT_GROUP_LOAD_FAILURE", "요금제 혜택 그룹 ID 확인에 실패했습니다."),

    BASIC_RECOMMENDATION_FAILURE(30010, "BASIC_RECOMMENDATION_FAILURE", "기본 추천 로직에 실패했습니다."),
    USER_SIMILAR_RECOMMENDATION_FAILURE(30011, "USER_SIMILARITY_CALCULATION_FAILURE", "사용자 유사도 기반 추천 로직에 실패했습니다."),
    USER_PLAN_SIMILAR_RECOMMENDATION_FAILURE(30012, "USER_PLAN_SIMILARITY_CALCULATION_FAILURE", "사용자 요금제 유사도 기반 추천 로직에 실패했습니다."),
    WEIGHTED_PLAN_RECOMMENDATION_FAILURE(30013, "WEIGHTED_PLAN_RECOMMENDATION_FAILURE", "가중치 기반 요금제 추천 로직에 실패했습니다."),
    COMBINE_AND_RANK_RECOMMENDATION_FAILURE(30014, "COMBINE_AND_RANK_RECOMMENDATION_FAILURE", "추천 결과 조합 및 최종 추천 결과 생성에 실패했습니다."),
    PLAN_SIMILARITY_RECOMMENDATION_FAILURE(30015, "PLAN_SIMILARITY_CALCULATION_FAILURE", "요금제 유사도 기반 추천 로직에 실패했습니다."),

    FEATURE_VECTOR_GENERATION_FAILURE(30020, "FEATURE_VECTOR_GENERATION_FAILURE", "특성 벡터 생성에 실패했습니다."),
    DATA_AVERAGE_CALCULATION_FAILURE(30021, "DATA_AVERAGE_CALCULATION_FAILURE", "사용자 데이터 기록 평균 계산에 실패했습니다."),
    NORMALIZATION_FAILURE(30022, "NORMALIZATION_FAILURE", "데이터 정규화에 실패했습니다."),
    PLAN_FILTER_FAILURE(30023, "PLAN_FILTER_FAILURE", "요금제 필터링에 실패했습니다."),
    WEIGHTED_SCORE_CALCULATION_FAILURE(30024, "WEIGHTED_SCORE_CALCULATION_FAILURE", "가중치 점수 계산에 실패했습니다."),
    SIMILARITY_CALCULATION_FAILURE(30025, "SIMILARITY_CALCULATION_FAILURE", "유사도 계산에 실패했습니다."),
    UNIT_CONVERSION_FAILURE(30026, "UTILITY_CONVERSION_FAILURE", "데이터 단위 변환에 실패했습니다."),
    FEEDBACK_REFLECTION_FAILURE(30027, "FEEDBACK_REFLECTION_FAILURE", "피드백 반영에 실패했습니다.");

    private final int code;
    private final String name;
    private final String message;
}
