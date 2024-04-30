package com.newgen.ntlsnc.multilayerapproval.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalActor;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 9/10/22
 * @time 4:55 PM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MultiLayerApprovalPath extends SuperEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ApprovalStepFeatureMap approvalStepFeatureMap;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ApprovalActor approvalActor;

    private Long approvalActorId; // ref Table  Role, Designation, Location Type, Application User

    @Transient
    private String approvalFeatureName;
}
