package com.comprehensive.eureka.recommend.service.impl;

import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.entity.UserPreference;
import com.comprehensive.eureka.recommend.repository.UserPreferenceRepository;
import com.comprehensive.eureka.recommend.service.UserPreferenceService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    @Override
    public void updateUserPreference(Long userId, UserPreferenceDto userPreferenceDto) {
        log.info("userId: {} updateUserPreference", userId);

        Optional<UserPreference> userPreferenceOptional = userPreferenceRepository.findByUserId(userId);

        if (userPreferenceOptional.isEmpty()) {
            log.info("userId: {} 의 통신 성향 정보가 존재하지 않습니다. 새로 생성합니다.", userId);
            UserPreference savedPreference = userPreferenceRepository.save(convertDtoToEntity(userPreferenceDto));

        } else {
            log.info("userId: {} 의 통신 성향 정보를 업데이트합니다.", userId);
            UserPreference existingPreference = userPreferenceOptional.get();

            existingPreference.update(userPreferenceDto);
        }
    }

    private UserPreference convertDtoToEntity(UserPreferenceDto userPreferenceDto) {
        return UserPreference.builder()
                .userId(userPreferenceDto.getUserId())
                .preferredDataAllowance(userPreferenceDto.getPreferenceDataUsage())
                .preferredDataUnit(userPreferenceDto.getPreferenceDataUsageUnit())
                .preferredSharedDataAllowance(userPreferenceDto.getPreferenceSharedDataUsage())
                .preferredSharedDataUnit(userPreferenceDto.getPreferenceSharedDataUsageUnit())
                .preferredPrice(userPreferenceDto.getPreferencePrice())
                .preferredBenefit(userPreferenceDto.getPreferenceBenefit())
                .preferredFamilyData(userPreferenceDto.isPreferenceFamilData())
                .preferredAdditionalCallAllowance(userPreferenceDto.getPreferenceValueAddedCallUsage())
                .build();
    }
}
