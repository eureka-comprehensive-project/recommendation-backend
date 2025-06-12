package com.comprehensive.eureka.recommend.service;

import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import java.util.List;

public interface UserPreferenceService {

    void updateUserPreference(Long userId, UserPreferenceDto userPreferenceDto);
    List<UserPreferenceDto> getAllUserPreferences();
}
