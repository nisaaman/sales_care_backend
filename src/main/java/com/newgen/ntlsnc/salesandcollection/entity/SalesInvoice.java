package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.CalculationType;
import com.newgen.ntlsnc.globalsettings.entity.Document;
import com.newgen.ntlsnc.globalsettings.entity.InvoiceNature;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.TermsAndConditions;
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
public class SalesInvoice extends SuperEntity<Long> {

    @Column(nullable = false)
    private String invoiceNo;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    @Column(nullable = false, scale = 4)
    private Float invoiceAmount = 0.0000F; // (invoiceAmount -  discountAmount + vatAmount -invoiceDiscount)

    @Column(nullable = false, scale = 4)
    private Float vatAmount = 0.0000F;

    @Column(nullable = false, scale = 4)
    private Float ordAmount = 0.0000F; // For payment adjustment except ord amount

    @Column(nullable = false, scale = 4)
    private Float discountAmount = 0.0000F; // only for report. comes from product trade discount

    @Column(nullable = false, scale = 4)
    private Float invoiceDiscount = 0.0000F; // comes from Invoice create page.

    @Enumerated(EnumType.STRING)
    private CalculationType invoiceDiscountType; // comes from Invoice create page.

    @Column(nullable = false)
    private Boolean isInvoiceDiscountReturned = false; // will be true when first sales return will be done


    @Column(nullable = false, scale = 4)
    private Float remainingAmount = 0.0000F; // (invoiceAmount - remainingAmount) >= vatAmount

    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvoiceNature invoiceNature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    @ManyToOne(fetch = FetchType.LAZY)
    private TermsAndConditions termsAndConditions;

    // Invoice Acknowledge
    private Boolean isAccepted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    private ApplicationUser acceptedBy;

    private LocalDate acceptanceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distributor_id", nullable = false)
    private Distributor distributor;
}
