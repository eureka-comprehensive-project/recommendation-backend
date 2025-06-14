package com.comprehensive.eureka.recommend.entity;

import com.comprehensive.eureka.recommend.dto.UserPreferenceDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_preference")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long preferenceId;

    private Long userId;

    private Integer preferredDataAllowance;

    private String preferredDataUnit;

    private Integer preferredSharedDataAllowance;

    private String preferredSharedDataUnit;

    private Integer preferredPrice;

    private Long preferredBenefitGroupId;

    private boolean preferredFamilyData;

    private Integer preferredAdditionalCallAllowance;

    public void update(UserPreferenceDto dto) {
        this.preferredDataAllowance = dto.getPreferenceDataUsage();
        this.preferredDataUnit = dto.getPreferenceDataUsageUnit();
        this.preferredSharedDataAllowance = dto.getPreferenceSharedDataUsage();
        this.preferredSharedDataUnit = dto.getPreferenceSharedDataUsageUnit();
        this.preferredPrice = dto.getPreferencePrice();
        this.preferredBenefitGroupId = dto.getPreferenceBenefitGroupId();
        this.preferredFamilyData = dto.isPreferenceFamilyData();
        this.preferredAdditionalCallAllowance = dto.getPreferenceValueAddedCallUsage();
    }
}
