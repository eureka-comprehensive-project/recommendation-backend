package com.comprehensive.eureka.recommend.service;

import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;

public interface UserPreferenceService {

    void updateUserPreference(Long userId, UserPreferenceDto userPreferenceDto);
}
