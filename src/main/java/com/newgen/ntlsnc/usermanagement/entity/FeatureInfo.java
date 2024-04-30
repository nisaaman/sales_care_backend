package com.newgen.ntlsnc.usermanagement.entity;

import com.newgen.ntlsnc.common.BaseEntity;
import com.newgen.ntlsnc.common.enums.ActivityFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author liton
 * Created on 9/21/22 12:37 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class FeatureInfo extends BaseEntity<Long> {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityFeature activityFeature;

    @Column(nullable = false)
    private String featureName;

    @Column(nullable = false)
    private String controllerName;

    @Column(nullable = false)
    private String menuUrl;

}
