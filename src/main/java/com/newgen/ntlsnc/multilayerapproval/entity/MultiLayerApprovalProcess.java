package com.newgen.ntlsnc.multilayerapproval.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalActor;
import com.newgen.ntlsnc.common.enums.ApprovalFeature;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 9/14/22
 * @time 2:40 PM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MultiLayerApprovalProcess extends SuperEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private MultiLayerApprovalPath multiLayerApprovalPath;

    @Column(nullable = false)
    private String refTable; // SalesBooking

    @Column(nullable = false)
    private Long refId; //  1

    private String comments;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ApplicationUser actionTakenBy;

    // only for report purpose

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ApprovalActor approvalActor;

    private Long approvalActorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ApprovalStep approvalStep;

    private Integer level;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ApprovalFeature approvalFeature;

    private Boolean isNextLayerDone = Boolean.FALSE;

}
