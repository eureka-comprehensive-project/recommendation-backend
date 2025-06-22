package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.FeedbackConstant;
import com.comprehensive.eureka.recommend.constant.WeightConstant;
import com.comprehensive.eureka.recommend.dto.BenefitDto;
import com.comprehensive.eureka.recommend.dto.FeedbackDto;
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

    public double calculateWeightedScore(UserPreferenceDto userPref, double avgDataUsage, PlanDto plan, FeedbackDto feedbackDto) {
        double score = 0.0;
        score += getDataScore(userPref, avgDataUsage, plan, feedbackDto);
        score += getPriceScore(userPref, plan, feedbackDto);
        score += getSharedDataScore(userPref, plan);
        score += getBenefitScore(userPref, plan, feedbackDto);
        score += getValueAddedCallScore(userPref, plan);
        score += getFamilyDataScore(userPref, plan);
        return score;
    }

    public double calculateSufficiencyScore(PlanDto plan, UserPreferenceDto userPreference) {
        double preferenceData = UnitConverter.convertToGigabytes(
                userPreference.getPreferenceDataUsage(),
                userPreference.getPreferenceDataUsageUnit()
        );

        double planData = UnitConverter.convertToGigabytes(
                plan.getDataAllowance(),
                plan.getDataAllowanceUnit(),
                plan.getDataPeriod()
        );

        if (planData < (preferenceData * 0.95)) return 0.3;

        double dataDiff = planData - preferenceData;
        double dataScore;

        if (dataDiff > 0) dataScore = Math.exp(-0.003 * dataDiff);
        else dataScore = Math.exp(-0.01 * dataDiff);

        double preferencePrice = userPreference.getPreferencePrice().doubleValue();
        double planPrice = plan.getMonthlyFee().doubleValue();

        double priceScore = 1.0;
        if (planPrice > preferencePrice) {
            double priceDiffRatio = (planPrice - preferencePrice) / preferencePrice;
            priceScore = Math.exp(-1.5 * priceDiffRatio);
        }

        return (dataScore * 0.6) + (priceScore * 0.4);
    }

    private double getDataScore(UserPreferenceDto userPref, double avgDataUsage, PlanDto plan, FeedbackDto feedbackDto) {
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

        if (feedbackDto != null && (feedbackDto.getDetailCode() == 1 || feedbackDto.getDetailCode() == 2)) {
            if (feedbackDto.getSentimentCode() == 3) {
                return score * (WeightConstant.PREFERENCE_DATA_USAGE_WEIGHT + FeedbackConstant.DATA_ADJUSTMENT_RATE_ANGRY);
            } else {
                return score * (WeightConstant.PREFERENCE_DATA_USAGE_WEIGHT + FeedbackConstant.DATA_ADJUSTMENT_RATE);
            }
        } else {
            return score * WeightConstant.PREFERENCE_DATA_USAGE_WEIGHT;
        }
    }

    private double getPriceScore(UserPreferenceDto userPref, PlanDto plan, FeedbackDto feedbackDto) {
        if (userPref.getPreferencePrice() == null || plan.getMonthlyFee() == null) return 0.0;

        double score = scoringRule.calculatePriceScore(userPref.getPreferencePrice(), plan.getMonthlyFee());

        if (feedbackDto != null && (feedbackDto.getDetailCode() == 3 || feedbackDto.getDetailCode() == 4)) {
            if (feedbackDto.getSentimentCode() == 3) {
                return score * (WeightConstant.PREFERENCE_PRICE_WEIGHT + FeedbackConstant.PRICE_ADJUSTMENT_RATE_ANGRY);
            } else {
                return score * (WeightConstant.PREFERENCE_PRICE_WEIGHT + FeedbackConstant.PRICE_ADJUSTMENT_RATE);
            }
        } else {
            return score * WeightConstant.PREFERENCE_PRICE_WEIGHT;
        }
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

    private double getBenefitScore(UserPreferenceDto userPref, PlanDto plan, FeedbackDto feedbackDto) {
        if (userPref.getPreferenceBenefitGroupId() == null) {
            return 0.0;
        }
        double score = scoringRule.calculateBenefitScore(userPref.getPreferenceBenefitGroupId(), plan);

        if (feedbackDto != null && feedbackDto.getDetailCode() == 5) {
            if (feedbackDto.getSentimentCode() == 3) {
                return score * (WeightConstant.PREFERENCE_BENEFITS_WEIGHT + FeedbackConstant.BENEFIT_ADJUSTMENT_RATE_ANGRY);
            } else {
                return score * (WeightConstant.PREFERENCE_BENEFITS_WEIGHT + FeedbackConstant.BENEFIT_ADJUSTMENT_RATE);
            }
        } else {
            return score * WeightConstant.PREFERENCE_BENEFITS_WEIGHT;
        }
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