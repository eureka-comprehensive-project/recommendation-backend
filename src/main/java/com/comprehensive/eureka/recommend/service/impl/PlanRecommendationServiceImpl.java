package com.comprehensive.eureka.recommend.service.impl;

import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendationDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.UserDataRecordResponseDto;
import com.comprehensive.eureka.recommend.entity.UserPreference;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.repository.UserPreferenceRepository;
import com.comprehensive.eureka.recommend.service.PlanRecommendationService;
import com.comprehensive.eureka.recommend.service.engine.BasicRecommender;
import com.comprehensive.eureka.recommend.service.engine.UserPlanSimilarRecommender;
import com.comprehensive.eureka.recommend.service.util.PlanFilter;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import com.comprehensive.eureka.recommend.util.api.UserApiServiceClient;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanRecommendationServiceImpl implements PlanRecommendationService {

    private final UserPreferenceRepository userPreferenceRepository;

    private final PlanApiServiceClient planApiServiceClient;
    private final UserApiServiceClient userApiServiceClient;

    private final BasicRecommender basicRecommender;
    private final UserPlanSimilarRecommender userPlanSimilarRecommender;

    private final PlanFilter planFilter;

    @Override
    public List<RecommendationDto> recommendPlan(Long userId) {
        List<PlanDto> allPlans = fetchAllPlans();
        UserPreferenceDto userPreference = fetchUserPreference(userId);
        int userAge = getUserAge(fetchUserBirthDay(userId));

        if (userPreference.getPreferenceDataUsage() == null && userPreference.getPreferencePrice() == null
                && userPreference.getPreferenceSharedDataUsage() == null) {

            log.info("[추천 로직 시작] userId: {} 에 대한 사용자 선호 정보가 없어, 기본 추천 로직을 실행합니다.", userId);
            return basicRecommender.recommendPlansByAge(allPlans, userId, userAge);

        } else {
            log.info("[추천 로직 시작] userId: {} 에 대한 사용자 선호 기반 추천 로직을 실행합니다.", userId);

            List<PlanDto> targetPlans = planFilter.filterPlansByAge(allPlans, userAge);
            List<UserDataRecordResponseDto> userDataHistory = fetchUserDataRecords(userId);

            List<RecommendationDto> userPlanSimilarityResults = userPlanSimilarRecommender.recommendByUserPlanSimilarity(userPreference, userDataHistory, targetPlans);

            return null;
        }
    }

    private Integer getUserAge(LocalDate birthDay) {
        LocalDate today = LocalDate.now();
        return Period.between(birthDay, today).getYears();
    }

    private List<PlanDto> fetchAllPlans() {
        try {
            return planApiServiceClient.getAllPlans();

        } catch (Exception e) {
            log.error("[외부 API 호출 실패] 전체 요금제 정보 호출에 실패했습니다.", e);
            throw new RecommendationException(ErrorCode.PLAN_LOAD_FAILURE);
        }
    }

    private UserPreferenceDto fetchUserPreference(Long userId) {
        Optional<UserPreference> userPreferenceOptional = userPreferenceRepository.findByUserId(userId);

        if (userPreferenceOptional.isEmpty()) {
            log.error("[사용자 선호 정보 조회 실패] userId: {} 에 대한 사용자 선호 정보를 찾을 수 없습니다.", userId);
            throw new RecommendationException(ErrorCode.USER_PREFERENCE_LOAD_FAILURE);
        }

        return convertEntityToDto(userPreferenceOptional.get());
    }

    private LocalDate fetchUserBirthDay(Long userId) {
        try {
            return userApiServiceClient.getUserBirthDay(userId);

        } catch (Exception e) {
            log.error("[외부 API 호출 실패] userId: {} 의 생년월일 정보 호출에 실패했습니다.", userId, e);
            throw new RecommendationException(ErrorCode.USER_LOAD_FAILURE);
        }
    }

    private List<UserDataRecordResponseDto> fetchUserDataRecords(Long userId) {
        try {
            return userApiServiceClient.getUserDataRecords(userId);

        } catch (Exception e) {
            log.error("[외부 API 호출 실패] userId: {} 의 데이터 기록 정보 호출에 실패했습니다.", userId, e);
            throw new RecommendationException(ErrorCode.USER_DATA_RECORD_LOAD_FAILURE);
        }
    }

    private UserPreferenceDto convertEntityToDto(UserPreference userPreference) {
        if (userPreference == null) {
            return null;
        }

        return UserPreferenceDto.builder()
                .userId(userPreference.getUserId())
                .preferenceDataUsage(userPreference.getPreferredDataAllowance())
                .preferenceDataUsageUnit(userPreference.getPreferredDataUnit())
                .preferenceSharedDataUsage(userPreference.getPreferredSharedDataAllowance())
                .preferenceSharedDataUsageUnit(userPreference.getPreferredSharedDataUnit())
                .preferencePrice(userPreference.getPreferredPrice())
                .preferenceBenefit(userPreference.getPreferredBenefit())
                .isPreferenceFamilData(userPreference.isPreferredFamilyData())
                .preferenceValueAddedCallUsage(userPreference.getPreferredAdditionalCallAllowance())
                .build();
    }
}
