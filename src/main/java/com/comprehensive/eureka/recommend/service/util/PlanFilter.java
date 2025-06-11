package com.comprehensive.eureka.recommend.service.util;

import com.comprehensive.eureka.recommend.constant.CategoryConstant;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PlanFilter {

    public List<PlanDto> filterPlansByAge(List<PlanDto> allPlans, int userAge) {
        Set<String> targetCategories;

        if (userAge < 13) {
            targetCategories = Set.of(CategoryConstant.CATEGORY_KIDS, CategoryConstant.CATEGORY_5G_GENERAL);
        } else if (userAge < 20) {
            targetCategories = Set.of(CategoryConstant.CATEGORY_TEEN, CategoryConstant.CATEGORY_5G_GENERAL);
        } else if (userAge < 30) {
            targetCategories = Set.of(CategoryConstant.CATEGORY_YOUTH, CategoryConstant.CATEGORY_5G_GENERAL);
        } else if (userAge < 65) {
            targetCategories = Set.of(CategoryConstant.CATEGORY_5G_GENERAL);
        } else {
            targetCategories = Set.of(CategoryConstant.CATEGORY_SENIOR, CategoryConstant.CATEGORY_5G_GENERAL);
        }

        return allPlans.stream()
                .filter(plan -> targetCategories.contains(plan.getCategory()))
                .toList();
    }
}
