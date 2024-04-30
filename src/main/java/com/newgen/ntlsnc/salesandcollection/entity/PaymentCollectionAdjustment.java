package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.supplychainmanagement.entity.SalesReturn;
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
public class PaymentCollectionAdjustment extends SuperEntity<Long> {

    @Column(nullable = false)
    private Float adjustedAmount = 0.0F;

    @Column(nullable = false)
    private Float ordAmount = 0.0F;

    @Column(nullable = false)
    private Boolean isOrdSettled = Boolean.FALSE;

    @Column(nullable = false)
    private LocalDate mappingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesInvoice salesInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private PaymentCollection paymentCollection;

    @ManyToOne(fetch = FetchType.LAZY)
    private DistributorBalance distributorBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;
}
