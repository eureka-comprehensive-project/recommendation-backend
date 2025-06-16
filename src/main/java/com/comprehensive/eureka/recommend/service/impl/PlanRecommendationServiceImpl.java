package com.comprehensive.eureka.recommend.service.impl;

import com.comprehensive.eureka.recommend.dto.BenefitDto;
import com.comprehensive.eureka.recommend.dto.FeedbackDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.response.RecommendationResponseDto;
import com.comprehensive.eureka.recommend.dto.response.UserDataRecordResponseDto;
import com.comprehensive.eureka.recommend.entity.UserPreference;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.repository.UserPreferenceRepository;
import com.comprehensive.eureka.recommend.service.PlanRecommendationService;
import com.comprehensive.eureka.recommend.service.engine.BasicRecommender;
import com.comprehensive.eureka.recommend.service.engine.UserPlanSimilarRecommender;
import com.comprehensive.eureka.recommend.service.engine.UserSimilarRecommender;
import com.comprehensive.eureka.recommend.service.engine.WeightRecommender;
import com.comprehensive.eureka.recommend.service.util.DataRecordAvgCalculator;
import com.comprehensive.eureka.recommend.service.util.PlanFilter;
import com.comprehensive.eureka.recommend.service.util.RecommendationCombineAndRanker;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import com.comprehensive.eureka.recommend.util.api.UserApiServiceClient;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
    private final UserSimilarRecommender userSimilarRecommender;
    private final WeightRecommender weightRecommender;
    private final RecommendationCombineAndRanker recommendationCombineAndRanker;
    private final DataRecordAvgCalculator dataRecordAvgCalculator;

    private final PlanFilter planFilter;

    @Override
    public RecommendationResponseDto recommendPlan(Long userId, FeedbackDto feedbackDto) {
        List<PlanDto> allPlans = fetchAllPlans();
        UserPreferenceDto userPreference = fetchUserPreference(userId);
        int userAge = getUserAge(fetchUserBirthDay(userId));
        double avgDataUsage = dataRecordAvgCalculator.calculateAverageDataUsage(fetchUserDataRecords(userId));

        if (userPreference.getPreferenceDataUsage() == null && userPreference.getPreferencePrice() == null) {

            // 사용자 나이에 맞는 요금제 중에서 비싼 요금제 3개 추천
            log.info("[추천 로직 시작] userId: {} 에 대한 사용자 선호 정보가 없어, 기본 추천 로직을 실행합니다.", userId);
            return basicRecommender.recommendPlansByAge(allPlans, userPreference, avgDataUsage, userAge);

        } else {
            log.info("[추천 로직 시작] userId: {} 에 대한 사용자 선호 기반 추천 로직을 실행합니다.", userId);

            List<PlanDto> targetPlans = planFilter.filterPlansByAge(allPlans, userAge);
            List<UserDataRecordResponseDto> userDataHistory = fetchUserDataRecords(userId);

            // 1번 사용자 통신성향과 나이에 맞는 요금제 유사도
            // 디비에 저장되어 있는 사용자 통신성향을 추출
            // 추천을 원하는 사용자의 인원의 나이를 통해 특정 요금제를 추출
            // 통신 성향과 > (정규화) 요금제를 비교 해서 추천
            List<RecommendPlanDto> userPlanSimilarityResults = userPlanSimilarRecommender.recommendByUserPlanSimilarity(
                    userPreference, userDataHistory, targetPlans);

            // 2번 사용자간 유사도
            // 나의 통신 성향과 다른 사람들의 통신 성향을 비교
            // 유사한 성향을 가진 유저를 찾아서 해당 유저가 어떤 요금제를 사용하는지 판단
            // 해당 요금제 추천
            List<RecommendPlanDto> userSimilarityResults = userSimilarRecommender.recommendBySimilarUsers(userId,
                    userPreference, userDataHistory, targetPlans);

            // 3번 사용자 통신 성향에 가중치 부여 후 점수 계산
            // 디비에서 사용자 통신성향 추출 -> 통신 섷향의 각 필드에 가중치 부여
            // 통신성향의 각 필드와 요금제의 각 필드를 비교하면서 점수 부여
            // 가중치가 높은 필드에 대해서는 점수 부여를 더 크게 함
            List<RecommendPlanDto> weightedResults = weightRecommender.recommendByWeightScore(userPreference,
                    targetPlans, userDataHistory, feedbackDto);

            return recommendationCombineAndRanker.combineAndRankRecommendations(
                    userSimilarityResults,
                    weightedResults,
                    userPlanSimilarityResults,
                    userPreference,
                    avgDataUsage
            );
        }
    }

    @Override
    public List<RecommendPlanDto> recommendPlanByKeyword(String keyword) {
        log.info("[키워드 추천 로직 시작] keyword: {}", keyword);
        List<PlanDto> allPlans = fetchAllPlans();

        List<PlanDto> filteredPlans = switch (keyword) {
            case "대학생", "20대", "유스", "유쓰" -> allPlans.stream()
                    .filter(plan -> plan.getPlanCategory().contains("유스"))
                    .sorted(Comparator.comparing(PlanDto::getMonthlyFee).reversed())
                    .limit(3)
                    .toList();
            case "청소년", "10대", "중학생", "고등학생", "학생" -> allPlans.stream()
                    .filter(plan -> plan.getPlanCategory().contains("청소년"))
                    .sorted(Comparator.comparing(PlanDto::getMonthlyFee).reversed())
                    .limit(3)
                    .toList();
            case "어린이", "초등학생", "키즈", "유치원", "유치원생", "아이" -> allPlans.stream()
                    .filter(plan -> plan.getPlanName().contains("키즈"))
                    .sorted(Comparator.comparing(PlanDto::getMonthlyFee).reversed())
                    .limit(3)
                    .toList();
            case "시니어", "노인", "65세 이상", "부모님", "어르신" -> allPlans.stream()
                    .filter(plan -> plan.getPlanCategory().contains("시니어"))
                    .sorted(Comparator.comparing(PlanDto::getMonthlyFee).reversed())
                    .limit(3)
                    .toList();
            default -> allPlans.stream()
                    .filter(plan -> plan.getPlanCategory().contains("프리미엄"))
                    .sorted(Comparator.comparing(PlanDto::getMonthlyFee).reversed())
                    .limit(3)
                    .toList();
        };

        log.info("[키워드 추천 로직 완료] keyword: {}", keyword);

        return filteredPlans.stream()
                .map(plan -> RecommendPlanDto.builder()
                        .plan(plan)
                        .recommendationType("KEYWORD")
                        .build())
                .peek(recommendPlanDto -> {
                    List<BenefitDto> benefits = fetchBenefitsByPlanId(recommendPlanDto.getPlan().getPlanId());
                    recommendPlanDto.setBenefits(benefits);
                })
                .collect(Collectors.toList());
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

    private List<BenefitDto> fetchBenefitsByPlanId(Integer planId) {
        try {
            return planApiServiceClient.getBenefitsByPlanId(planId);

        } catch (Exception e) {
            log.error("[외부 API 호출 실패] 요금제 혜택 정보 호출에 실패했습니다.", e);
            throw new RecommendationException(ErrorCode.PLAN_BENEFIT_LOAD_FAILURE);
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
                .preferenceBenefitGroupId(userPreference.getPreferredBenefitGroupId())
                .isPreferenceFamilyData(userPreference.isPreferredFamilyData())
                .preferenceValueAddedCallUsage(userPreference.getPreferredAdditionalCallAllowance())
                .build();
    }
}
