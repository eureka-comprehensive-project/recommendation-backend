package com.comprehensive.eureka.recommend.service;

import com.comprehensive.eureka.recommend.dto.FeedbackDto;
import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import java.util.List;

public interface UserPreferenceService {

    void updateUserPreference(Long userId, UserPreferenceDto userPreferenceDto);
    void updateUserPreference(Long userId, FeedbackDto feedbackDto);
    List<UserPreferenceDto> getAllUserPreferences();
    UserPreferenceDto getUserPreferenceById(Long userId);
}
