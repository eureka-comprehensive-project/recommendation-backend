package com.comprehensive.eureka.recommend.util.api;

import com.comprehensive.eureka.recommend.constant.DomainConstant;
import com.comprehensive.eureka.recommend.dto.BenefitDto;
import com.comprehensive.eureka.recommend.dto.PlanDto;
import com.comprehensive.eureka.recommend.dto.base.BaseResponseDto;
import com.comprehensive.eureka.recommend.util.RestUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanApiServiceClient {

    private final RestUtil restUtil;

    public List<PlanDto> getAllPlans() {
        BaseResponseDto<List<PlanDto>> response = restUtil.get(
                DomainConstant.PLAN_DOMAIN,
                new ParameterizedTypeReference<BaseResponseDto<List<PlanDto>>>() {}
        );

        return response.getData();
    }

    public List<BenefitDto> getBenefitsByPlanId(Integer planId) {
        BaseResponseDto<List<BenefitDto>> response = restUtil.get(
                DomainConstant.PLAN_DOMAIN + planId + "/benefits",
                new ParameterizedTypeReference<BaseResponseDto<List<BenefitDto>>>() {}
        );

        return response.getData();
    }
}
