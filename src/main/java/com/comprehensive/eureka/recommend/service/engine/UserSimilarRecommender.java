package com.comprehensive.eureka.recommend.service.engine;

import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.RecommendationDto;
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
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSimilarRecommender {
    private final UserPreferenceService userPreferenceService;

    private UserApiServiceClient userApiServiceClient;
    private PlanApiServiceClient planApiServiceClient;

    private final DataRecordAvgCalculator dataRecordAvgCalculator;
    private final FeatureVectorGenerator featureVectorGenerator;
    private final SimilarityCalculator similarityCalculator;

    private record SimilarUserResult(Long userId, double similarity, UserPreferenceDto preference) {}

    public List<RecommendationDto> recommendBySimilarUsers(
            Long targetUserId,
            UserPreferenceDto targetUserPreference,
            List<UserDataRecordResponseDto> targetUserHistory,
            List<PlanDto> allPlans
    ) {
        List<SimilarUserResult> similarUsers = findSimilarUsers(targetUserId, targetUserPreference, targetUserHistory);
        Map<Integer, Double> averagePlanScores = calculateAveragePlanScores(similarUsers);

        return buildRecommendations(averagePlanScores, allPlans);
    }

    private List<SimilarUserResult> findSimilarUsers(
            Long targetUserId,
            UserPreferenceDto targetUserPreference,
            List<UserDataRecordResponseDto> targetUserHistory
    ) {
        List<UserPreferenceDto> allPreferences = userPreferenceService.getAllUserPreferences();

        double targetAvgDataUsage = dataRecordAvgCalculator.calculateAverageDataUsage(targetUserHistory);
        double[] targetFeatureVector = featureVectorGenerator.createUserFeatureVector(targetUserPreference, targetAvgDataUsage);

        return allPreferences.stream()
                .filter(pref -> !pref.getUserId().equals(targetUserId))
                .map(pref -> {
                    List<UserDataRecordResponseDto> userHistory = fetchUserHistory(pref.getUserId());

                    double userAvgDataUsage = dataRecordAvgCalculator.calculateAverageDataUsage(userHistory);
                    double[] userFeatureVector = featureVectorGenerator.createUserFeatureVector(pref, userAvgDataUsage);

                    double similarity = similarityCalculator.calculateCosineSimilarity(targetFeatureVector,
                            userFeatureVector);

                    return new SimilarUserResult(pref.getUserId(), similarity, pref);
                })
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

    private List<RecommendationDto> buildRecommendations(Map<Integer, Double> averagePlanScores, List<PlanDto> allPlans) {
        return averagePlanScores.entrySet().stream()
                .sorted(Entry.<Integer, Double>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    PlanDto plan = allPlans.stream().filter(p -> p.getPlanId().equals(entry.getKey()))
                            .findFirst()
                            .orElseThrow(() -> new RecommendationException(ErrorCode.PLAN_LOAD_FAILURE));

                    return RecommendationDto.builder()
                            .plan(plan)
                            .score(entry.getValue())
                            .recommendationType("USER_SIMILARITY")
                            .build();
                })
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