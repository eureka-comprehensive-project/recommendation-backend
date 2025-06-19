package com.comprehensive.eureka.recommend.service.impl;

import com.comprehensive.eureka.recommend.constant.FeedbackConstant;
import com.comprehensive.eureka.recommend.dto.FeedbackDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import com.comprehensive.eureka.recommend.entity.UserPreference;
import com.comprehensive.eureka.recommend.exception.ErrorCode;
import com.comprehensive.eureka.recommend.exception.RecommendationException;
import com.comprehensive.eureka.recommend.repository.UserPreferenceRepository;
import com.comprehensive.eureka.recommend.service.UserPreferenceService;
import com.comprehensive.eureka.recommend.service.util.UnitConverter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    @Override
    @Transactional
    public void updateUserPreference(Long userId, UserPreferenceDto userPreferenceDto) {
        log.info("userId: {} updateUserPreference", userId);

        Optional<UserPreference> userPreferenceOptional = userPreferenceRepository.findByUserId(userId);

        if (userPreferenceOptional.isEmpty()) {
            log.info("userId: {} 의 통신 성향 정보가 존재하지 않습니다. 새로 생성합니다.", userId);
            UserPreference savedPreference = userPreferenceRepository.save(convertDtoToEntity(userId, userPreferenceDto));

        } else {
            log.info("userId: {} 의 통신 성향 정보를 업데이트합니다.", userId);
            UserPreference existingPreference = userPreferenceOptional.get();

            existingPreference.update(userPreferenceDto);
        }
    }

    @Override
    @Transactional
    public void updateUserPreference(Long userId, FeedbackDto feedbackDto) {
        log.info("userId: {} updateUserPreference by feedback", userId);

        Optional<UserPreference> userPreferenceOptional = userPreferenceRepository.findByUserId(userId);

        if (userPreferenceOptional.isEmpty()) {
            log.warn("userId: {} 의 통신 성향 정보가 존재하지 않습니다. 피드백을 반영할 수 없습니다.", userId);
            throw new RecommendationException(ErrorCode.FEEDBACK_REFLECTION_FAILURE);

        } else {
            UserPreference existingPreference = userPreferenceOptional.get();

            if (feedbackDto.getSentimentCode() == 2) {
                log.info("피드백(불만족)에 따라 userId: {} 의 통신 성향을 조정합니다. DetailCode: {}", userId, feedbackDto.getDetailCode());
                adjustPreferenceByFeedback(existingPreference, feedbackDto.getDetailCode());
            } else {
                log.info("피드백(만족)을 수신했습니다. userId: {} 의 통신 성향은 변경되지 않습니다.", userId);
            }

            log.info("userId: {} 의 통신 성향 정보를 피드백에 따라 업데이트했습니다.", userId);
        }
    }

    @Override
    public List<UserPreferenceDto> getAllUserPreferences() {
        log.info("getAllUserPreferences");

        List<UserPreference> allUserPreferences = userPreferenceRepository.findAll();
        return allUserPreferences.stream()
                .map(this::convertEntityToDto)
                .toList();
    }

    @Override
    public UserPreferenceDto getUserPreferenceById(Long userId) {
        log.info("getUserPreferenceById: {}", userId);

        Optional<UserPreference> userPreferenceOptional = userPreferenceRepository.findByUserId(userId);
        if (userPreferenceOptional.isPresent()) {
            return convertEntityToDto(userPreferenceOptional.get());
        } else {
            log.warn("userId: {} 의 통신 성향 정보가 존재하지 않습니다.", userId);
            return null;
        }
    }

    private void adjustPreferenceByFeedback(UserPreference preference, Long detailCode) {
        if (detailCode == 1) {
            if (preference.getPreferredDataAllowance() != null) {
                int currentData = preference.getPreferredDataAllowance();

                currentData = UnitConverter.convertToGigabytes(currentData, preference.getPreferredDataUnit())
                        .intValue();

                int adjustedData = (int) (currentData * FeedbackConstant.DATA_PLUS_ADJUSTMENT_RATE);
                preference.setPreferredDataAllowance(adjustedData);
                preference.setPreferredDataUnit("GB");

                log.info("데이터 부족 피드백: 선호 데이터 {} -> {}", currentData, adjustedData);
            }
        } else if (detailCode == 2) {
            if (preference.getPreferredDataAllowance() != null) {
                int currentData = preference.getPreferredDataAllowance();

                currentData = UnitConverter.convertToGigabytes(currentData, preference.getPreferredDataUnit())
                        .intValue();

                int adjustedData = (int) (currentData * FeedbackConstant.DATA_MINUS_ADJUSTMENT_RATE);
                preference.setPreferredDataAllowance(adjustedData);
                preference.setPreferredDataUnit("GB");

                log.info("데이터 과다 피드백: 선호 데이터 {} -> {}", currentData, adjustedData);
            }
        } else if (detailCode == 3) {
            if (preference.getPreferredPrice() != null) {
                int currentPrice = preference.getPreferredPrice();
                int adjustedPrice = (int) (currentPrice * FeedbackConstant.PRICE_MINUS_ADJUSTMENT_RATE);
                preference.setPreferredPrice(adjustedPrice);
                log.info("가격 비쌈 피드백: 선호 가격 {} -> {}", currentPrice, adjustedPrice);
            }
        } else if (detailCode == 4) {
            if (preference.getPreferredPrice() != null) {
                int currentPrice = preference.getPreferredPrice();
                int adjustedPrice = (int) (currentPrice * FeedbackConstant.PRICE_PLUS_ADJUSTMENT_RATE);
                preference.setPreferredPrice(adjustedPrice);
                log.info("가격 저렴 피드백: 선호 가격 {} -> {}", currentPrice, adjustedPrice);
            }
        } else if (detailCode == 5) {
            if (preference.getPreferredBenefitGroupId() == null || preference.getPreferredBenefitGroupId() == 0) {
                preference.setPreferredBenefitGroupId(1L);
            }
            log.info("부가혜택 부족 피드백: 부가혜택 가중치를 높입니다.");

        } else {
            log.warn("처리할 수 없는 피드백입니다: {}", detailCode);
        }
    }

    private UserPreference convertDtoToEntity(Long userId, UserPreferenceDto userPreferenceDto) {
        return UserPreference.builder()
                .userId(userId)
                .preferredDataAllowance(userPreferenceDto.getPreferenceDataUsage())
                .preferredDataUnit(userPreferenceDto.getPreferenceDataUsageUnit())
                .preferredSharedDataAllowance(userPreferenceDto.getPreferenceSharedDataUsage())
                .preferredSharedDataUnit(userPreferenceDto.getPreferenceSharedDataUsageUnit())
                .preferredPrice(userPreferenceDto.getPreferencePrice())
                .preferredBenefitGroupId(userPreferenceDto.getPreferenceBenefitGroupId())
                .preferredFamilyData(userPreferenceDto.isPreferenceFamilyData())
                .preferredAdditionalCallAllowance(userPreferenceDto.getPreferenceValueAddedCallUsage())
                .build();
    }

    private UserPreferenceDto convertEntityToDto(UserPreference userPreference) {
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
