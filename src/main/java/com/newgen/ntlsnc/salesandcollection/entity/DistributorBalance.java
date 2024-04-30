package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.globalsettings.entity.InvoiceNature;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author nisa
 * @date 5/16/22
 * @time 5:15 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DistributorBalance extends SuperEntity<Long> {

    @Column(nullable = false)
    private String referenceNo;

    @Column(nullable = false)
    private Float balance = 0.0F;

    @Column(nullable = false)
    private Float remainingBalance = 0.0F;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvoiceNature invoiceNature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Distributor distributor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;
}
