package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.CreditDebitTransactionType;
import com.newgen.ntlsnc.common.enums.NoteType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author liton
 * Created on 5/19/22 4:44 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CreditDebitNote extends SuperEntity<Long> {

    @Column(length = 30)
    private String noteNo;

    @Column(nullable = false)
    private LocalDate proposalDate;

    @Column(nullable = false)
    private Double amount = 0.0D;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private NoteType noteType;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CreditDebitTransactionType transactionType;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private LocalDateTime approvalDate;

    @Column(nullable = true)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesInvoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    private DistributorBalance distributorBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Distributor distributor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    @OneToOne
    private PaymentCollectionAdjustment paymentCollectionAdjustment;
}
