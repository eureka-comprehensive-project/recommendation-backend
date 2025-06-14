package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendPlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.dto.PlanBenefitDto;
import com.comprehensive.eureka.recommend.dto.response.UserDataRecordResponseDto;
import com.comprehensive.eureka.recommend.dto.response.UserPlanRecordResponseDto;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.service.UserPreferenceService;
import com.comprehensive.eureka.recommend.service.util.DataRecordAvgCalculator;
import com.comprehensive.eureka.recommend.service.util.FeatureVectorGenerator;
import com.comprehensive.eureka.recommend.service.util.SimilarityCalculator;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import com.comprehensive.eureka.recommend.util.api.UserApiServiceClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSimilarRecommender {
    private final UserPreferenceService userPreferenceService;

    private final UserApiServiceClient userApiServiceClient;
    private final PlanApiServiceClient planApiServiceClient;

    private final DataRecordAvgCalculator dataRecordAvgCalculator;
    private final FeatureVectorGenerator featureVectorGenerator;
    private final SimilarityCalculator similarityCalculator;

    private record SimilarUserResult(Long userId, double similarity, UserPreferenceDto preference) {}

    public List<RecommendPlanDto> recommendBySimilarUsers(
            Long targetUserId,
            UserPreferenceDto targetUserPreference,
            List<UserDataRecordResponseDto> targetUserHistory,
            List<PlanDto> targetPlans
    ) {
        log.info("사용자 간 유사도 기반 추천 로직 시작. 대상 사용자 ID: {}", targetUserId);
        try {
            List<SimilarUserResult> similarUsers = findSimilarUsers(targetUserId, targetUserPreference, targetUserHistory);

            if (similarUsers.isEmpty()) {
                log.warn("대상 사용자와 유사한 사용자를 찾을 수 없어 추천을 종료합니다.");
                return new ArrayList<>();
            }

            Map<Integer, Double> averagePlanScores = calculateAveragePlanScores(similarUsers);
            List<RecommendPlanDto> recommendations = buildRecommendations(averagePlanScores, targetPlans);
            log.info("사용자 간 유사도 기반 추천 로직 완료");

            return recommendations;

        } catch (RecommendationException e) {
            throw e;

        } catch (Exception e) {
            log.error("사용자 간 유사도 추천 프로세스 중 오류 발생. 사용자 ID: {}", targetUserId, e);
            throw new RecommendationException(ErrorCode.USER_SIMILAR_RECOMMENDATION_FAILURE);
        }
    }

    private List<SimilarUserResult> findSimilarUsers(
            Long targetUserId,
            UserPreferenceDto targetUserPreference,
            List<UserDataRecordResponseDto> targetUserHistory
    ) {
        List<UserPreferenceDto> allPreferences = userPreferenceService.getAllUserPreferences();
        log.debug("총 {}명의 사용자 선호 정보를 조회하여 유사도 계산", allPreferences.size() - 1);

        double targetAvgDataUsage = dataRecordAvgCalculator.calculateAverageDataUsage(targetUserHistory);
        double[] targetFeatureVector = featureVectorGenerator.createUserFeatureVector(targetUserPreference, targetAvgDataUsage);

        return allPreferences.parallelStream()
                .filter(pref -> !pref.getUserId().equals(targetUserId))
                .map(pref -> {
                    try {
                        List<UserDataRecordResponseDto> userHistory = fetchUserHistory(pref.getUserId());
                        double userAvgDataUsage = dataRecordAvgCalculator.calculateAverageDataUsage(userHistory);
                        double[] userFeatureVector = featureVectorGenerator.createUserFeatureVector(pref, userAvgDataUsage);
                        double similarity = similarityCalculator.calculateEuclideanSimilarity(targetFeatureVector, userFeatureVector);

                        log.trace("대상 사용자 ID: {} 와 비교 사용자 ID: {} 의 유사도: {}", targetUserId, pref.getUserId(), similarity);
                        return new SimilarUserResult(pref.getUserId(), similarity, pref);

                    } catch (Exception e) {
                        log.error("유사도 계산 중 사용자 ID: {} 처리 오류. 해당 사용자는 유사 사용자 목록에서 제외됩니다.", pref.getUserId(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
                .limit(20)
                .toList();
    }

    private Map<Integer, Double> calculateAveragePlanScores(List<SimilarUserResult> similarUsers) {
        Map<Integer, Double> planScores = new HashMap<>();
        Map<Integer, Integer> planCounts = new HashMap<>();

        List<Long> userIds = similarUsers.stream()
                .map(similarity -> similarity.userId)
                .collect(Collectors.toList());

        List<UserPlanRecordResponseDto> activeUserPlans = fetchActiveUserPlans(userIds);
        Map<Long, Long> userToPlanBenefitMap = activeUserPlans.stream()
                .filter(dto -> dto.getPlanBenefitId() != null)
                .collect(Collectors.toMap(
                        UserPlanRecordResponseDto::getUserId,
                        UserPlanRecordResponseDto::getPlanBenefitId,
                        (existing, replacement) -> existing
                ));

        List<Long> planBenefitIds = new ArrayList<>(userToPlanBenefitMap.values());
        List<PlanBenefitDto> planBenefits = fetchPlanBenefitsByPlanBenefitIds(planBenefitIds);
        Map<Long, Integer> benefitToPlanIdMap = planBenefits.stream()
                .collect(Collectors.toMap(PlanBenefitDto::getPlanBenefitId, PlanBenefitDto::getPlanId));

        similarUsers.forEach(similarity -> {
            Long planBenefitId = userToPlanBenefitMap.get(similarity.userId);

            if (planBenefitId != null) {
                Integer planId = benefitToPlanIdMap.get(planBenefitId);

                if (planId != null) {
                    planScores.merge(planId, similarity.similarity(), Double::sum);
                    planCounts.merge(planId, 1, Integer::sum);
                }
            }
        });

        planScores.replaceAll((planId, totalScore) -> totalScore / planCounts.get(planId));
        return planScores;
    }

    private List<RecommendPlanDto> buildRecommendations(Map<Integer, Double> averagePlanScores, List<PlanDto> targetPlans) {
        Map<Integer, PlanDto> planMap = targetPlans.stream()
                .collect(Collectors.toMap(PlanDto::getPlanId, Function.identity()));

        return averagePlanScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .map(entry -> {
                    PlanDto plan = planMap.get(entry.getKey());
                    if (plan == null) return null;

                    return RecommendPlanDto.builder()
                            .plan(plan)
                            .score(entry.getValue())
                            .recommendationType("USER_SIMILARITY")
                            .build();
                })
                .filter(Objects::nonNull)
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<UserDataRecordResponseDto> fetchUserHistory(Long userId) {
        try {
            return userApiServiceClient.getUserDataRecords(userId);

        } catch (Exception e) {
            log.error("[외부 API 호출 실패] userId: {} 의 사용 데이터 기록 호출에 실패했습니다.", userId, e);
            throw new RecommendationException(ErrorCode.USER_DATA_RECORD_LOAD_FAILURE);
        }
    }

    private List<PlanBenefitDto> fetchPlanBenefitsByPlanBenefitIds(List<Long> planBenefitIds) {
        try {
            return planApiServiceClient.getPlanBenefitsByPlanBenefitIds(planBenefitIds);

        } catch (Exception e) {
            log.error("[외부 API 호출 실패] 요금제 혜택 ID로 요금제 정보 호출에 실패했습니다.", e);
            throw new RecommendationException(ErrorCode.PLAN_LOAD_FAILURE);
        }
    }

    private List<UserPlanRecordResponseDto> fetchActiveUserPlans(List<Long> userIds) {
        try {
            return userApiServiceClient.getActiveUserPlans(userIds);

        } catch (Exception e) {
            log.error("[외부 API 호출 실패] 활성화 된 고객 요금제 호출에 실패했습니다.", e);
            throw new RecommendationException(ErrorCode.USER_PLAN_RECORD_LOAD_FAILURE);
        }
    }
}