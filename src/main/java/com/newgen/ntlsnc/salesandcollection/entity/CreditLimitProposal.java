package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.CreditLimitTerm;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author nisa
 * @date 4/7/22
 * @time 3:27 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CreditLimitProposal extends SuperEntity<Long> {

    @Column(nullable = false)
    private Float proposedAmount = 0.0f;

    @Column(nullable = false)
    private String proposalNo;

    private LocalDate proposalDate;

    private String proposalNotes;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CreditLimitTerm creditLimitTerm;

    private LocalDate startDate; // only for short term

    private LocalDate endDate; // only for short term

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesBooking salesBooking;  // only for a specific booking

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private LocalDate approvalDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Distributor distributor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;


    private Double businessPlan = 0.0d; //report purpose
    private Double currentLimit = 0.0d; //report purpose
}
