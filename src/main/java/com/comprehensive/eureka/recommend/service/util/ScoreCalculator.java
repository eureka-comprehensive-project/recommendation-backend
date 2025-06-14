package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.WeightConstant;
import com.comprehensive.eureka.recommend.dto.BenefitDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.util.api.PlanApiServiceClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreCalculator {

    private final ScoringRule scoringRule;
    private final PlanApiServiceClient planApiServiceClient;

    public double calculateWeightedScore(UserPreferenceDto userPref, double avgDataUsage, PlanDto plan) {
        double score = 0.0;
        score += getDataScore(userPref, avgDataUsage, plan);
        score += getPriceScore(userPref, plan);
        score += getSharedDataScore(userPref, plan);
        score += getBenefitScore(userPref, plan);
        score += getValueAddedCallScore(userPref, plan);
        score += getFamilyDataScore(userPref, plan);
        return score;
    }

    public double calculateSufficiencyScore(PlanDto plan, UserPreferenceDto userPreference) {
        double planData = UnitConverter.convertToGigabytes(
                plan.getDataAllowance(),
                plan.getDataAllowanceUnit(),
                plan.getDataPeriod()
        );
        double userPrefData = UnitConverter.convertToGigabytes(
                userPreference.getPreferenceDataUsage(),
                userPreference.getPreferenceDataUsageUnit()
        );

        boolean isSufficient = (planData == 0) || (planData >= userPrefData);

        if (isSufficient) return 1.0;
        else return (planData / userPrefData) - 0.2;
    }

    private double getDataScore(UserPreferenceDto userPref, double avgDataUsage, PlanDto plan) {
        if (userPref.getPreferenceDataUsage() == null || plan.getDataAllowance() == null) return 0.0;

        double preferredData = UnitConverter.convertToGigabytes(
                userPref.getPreferenceDataUsage(),
                userPref.getPreferenceDataUsageUnit()
        );

        double planData = UnitConverter.convertToGigabytes(
                plan.getDataAllowance(),
                plan.getDataAllowanceUnit(),
                plan.getDataPeriod()
        );

        double score = scoringRule.calculateDataScore(preferredData, avgDataUsage, planData);
        return score * WeightConstant.PREFERENCE_DATA_USAGE_WEIGHT;
    }

    private double getPriceScore(UserPreferenceDto userPref, PlanDto plan) {
        if (userPref.getPreferencePrice() == null || plan.getMonthlyFee() == null) return 0.0;

        double score = scoringRule.calculatePriceScore(userPref.getPreferencePrice(), plan.getMonthlyFee());
        return score * WeightConstant.PREFERENCE_PRICE_WEIGHT;
    }

    private double getSharedDataScore(UserPreferenceDto userPref, PlanDto plan) {
        if (userPref.getPreferenceSharedDataUsage() == null || plan.getTetheringDataAmount() == null) {
            return 0.0;
        }

        double preferredShared = UnitConverter.convertToGigabytes(
                userPref.getPreferenceSharedDataUsage(),
                userPref.getPreferenceSharedDataUsageUnit()
        );

        double planShared = UnitConverter.convertToGigabytes(
                plan.getTetheringDataAmount(),
                plan.getTetheringDataUnit()
        );

        double score = scoringRule.calculateSharedDataScore(preferredShared, planShared);
        return score * WeightConstant.PREFERENCE_SHARED_DATA_WEIGHT;
    }

    private double getBenefitScore(UserPreferenceDto userPref, PlanDto plan) {
        if (userPref.getPreferenceBenefit() == null) {
            return 0.0;
        }
        double score = scoringRule.calculateBenefitScore(userPref.getPreferenceBenefit(), fetchPlanBenefits(plan.getPlanId()));
        return score * WeightConstant.PREFERENCE_BENEFITS_WEIGHT;
    }

    private double getValueAddedCallScore(UserPreferenceDto userPref, PlanDto plan) {
        if (userPref.getPreferenceValueAddedCallUsage() == null || plan.getAdditionalCallAllowance() == null) {
            return 0.0;
        }

        double score = scoringRule.calculateValueAddedCallScore(
                userPref.getPreferenceValueAddedCallUsage(),
                plan.getAdditionalCallAllowance()
        );
        return score * WeightConstant.PREFERENCE_VALUE_ADDED_CALL_WEIGHT;
    }

    private double getFamilyDataScore(UserPreferenceDto userPref, PlanDto plan) {
        double score = scoringRule.calculateFamilyDataScore(
                userPref.isPreferenceFamilyData(),
                plan.isFamilyDataEnabled()
        );
        return score * WeightConstant.PREFERENCE_FAMILY_DATA_WEIGHT;
    }

    private List<BenefitDto> fetchPlanBenefits(Integer planId) {
        try {
            return planApiServiceClient.getBenefitsByPlanId(planId);

        } catch (Exception e) {
            log.error("[외부 API 호출 실패] planId: {} 의 혜택 정보 호출에 실패했습니다.", planId, e);
            throw new RecommendationException(ErrorCode.PLAN_BENEFIT_LOAD_FAILURE);
        }
    }
}
