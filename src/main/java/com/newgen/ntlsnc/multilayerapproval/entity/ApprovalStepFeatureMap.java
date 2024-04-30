package com.newgen.ntlsnc.multilayerapproval.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalFeature;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 9/10/22
 * @time 3:47 PM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ApprovalStepFeatureMap extends SuperEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ApprovalStep approvalStep;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ApprovalFeature approvalFeature;

    @Column(nullable = false)
    private Integer level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;
}
