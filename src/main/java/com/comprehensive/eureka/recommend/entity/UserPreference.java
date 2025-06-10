package com.comprehensive.eureka.recommend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
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

    private String preferredBenefit;

    private boolean preferredFamilyData;

    private Integer preferredAdditionalCallAllowance;
}
