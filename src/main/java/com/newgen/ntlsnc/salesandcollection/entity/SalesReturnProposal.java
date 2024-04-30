package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDeliveryChallan;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author marziah
 * @Date 13/04/22
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SalesReturnProposal extends SuperEntity<Long> {

    private String proposalNo;      // nullable true for add to cart mobile api

    private LocalDate proposalDate;    // nullable true for add to cart mobile api

    private String returnReason;       // nullable true for add to cart mobile api

    @Column(nullable = false)
    private Boolean isReturn = false;  // When Return is done then true. otherwise false

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private LocalDate approvalDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Distributor distributor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SalesInvoice salesInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ApplicationUser salesOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvDeliveryChallan deliveryChallan;
    // to filter selective range of invoices
    // and show to approvals screen and return-receive screen.
    private LocalDate invoiceFromDate;
    private LocalDate invoiceToDate;

    @Column
    private Boolean isProposalConvert = false;  // When Sales return proposal convert to other proposal then true. otherwise false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private SalesReturnProposal refSalesReturnProposal;


}
