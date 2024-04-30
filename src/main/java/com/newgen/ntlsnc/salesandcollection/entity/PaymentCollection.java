package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.PaymentNature;
import com.newgen.ntlsnc.common.enums.PaymentType;
import com.newgen.ntlsnc.globalsettings.entity.BankBranch;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.PaymentBook;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author marziah
 * @Date 13/04/22
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PaymentCollection extends SuperEntity<Long> {

    @Column(nullable = false)
    private String paymentNo;

    @Column(nullable = false)
    private String referenceNo;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private Double collectionAmount = 0.0;

    @Column(nullable = false)
    private Double remainingAmount = 0.0;

    private Long moneyReceiptNo;
    private String rejectReason;
    private LocalDateTime actionTakenDate;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentNature paymentNature;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private PaymentBook paymentBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ApplicationUser collectionBy; //so

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private ApplicationUser actionTakenBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Distributor distributor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_booking_id", nullable = true)
    private SalesBooking salesBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_branch_id")
    private BankBranch bankBranch;

    @Column(length = 20)  //Authorization process for Payment Nature Advance
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatusForAuthorization;

    private LocalDate approvalDateForAuthorization;
}
