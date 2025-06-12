package com.comprehensive.eureka.recommend.util.api;

import com.comprehensive.eureka.recommend.constant.DomainConstant;
import com.comprehensive.eureka.recommend.dto.BenefitDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.base.BaseResponseDto;
import com.comprehensive.eureka.recommend.dto.PlanBenefitDto;
import com.comprehensive.eureka.recommend.util.WebClientUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanApiServiceClient {

    private final WebClientUtil webClientUtil;

    public List<PlanDto> getAllPlans() {
        BaseResponseDto<List<PlanDto>> response = webClientUtil.get(
                DomainConstant.PLAN_DOMAIN + DomainConstant.SLASH + DomainConstant.PLAN_SERVICE_KEY + DomainConstant.SLASH,
                new ParameterizedTypeReference<BaseResponseDto<List<PlanDto>>>() {}
        );

        return response.getData();
    }

    public List<BenefitDto> getBenefitsByPlanId(Integer planId) {
        BaseResponseDto<List<BenefitDto>> response = webClientUtil.get(
                DomainConstant.PLAN_DOMAIN + DomainConstant.SLASH + DomainConstant.PLAN_SERVICE_KEY + DomainConstant.SLASH + planId + "/benefits",
                new ParameterizedTypeReference<BaseResponseDto<List<BenefitDto>>>() {}
        );

        return response.getData();
    }

    public List<PlanBenefitDto> getPlanBenefitsByPlanBenefitIds(List<Long> planBenefitIds) {
        BaseResponseDto<List<PlanBenefitDto>> response = webClientUtil.post(
                DomainConstant.PLAN_DOMAIN + DomainConstant.SLASH + DomainConstant.PLAN_SERVICE_KEY + DomainConstant.SLASH + "plan-benefit",
                planBenefitIds,
                new ParameterizedTypeReference<BaseResponseDto<List<PlanBenefitDto>>>() {}
        );

        return response.getData();
    }
}
