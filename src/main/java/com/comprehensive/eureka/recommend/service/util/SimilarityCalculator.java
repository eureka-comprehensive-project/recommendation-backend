package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.dto.BenefitDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SimilarityCalculator {

    public double calculateCosineSimilarity(double[] vector1, double[] vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }

        if (norm1 == 0.0 || norm2 == 0.0) return 0.0;

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public double calculateUserPlanBenefitSimilarity(String userBenefit, List<BenefitDto> planBenefits) {
        if (userBenefit == null || planBenefits == null || planBenefits.isEmpty()) {
            return 0.0;
        }

        boolean isMatch = planBenefits.stream()
                .anyMatch(benefitDto -> userBenefit.equals(benefitDto.getBenefitName()));

        return isMatch ? 1.0 : 0.5;
    }
}
