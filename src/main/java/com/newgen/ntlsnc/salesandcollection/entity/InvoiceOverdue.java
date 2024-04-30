package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.globalsettings.entity.InvoiceNature;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author Newaz Sharif
 * @since 8th Aug,22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class InvoiceOverdue extends SuperEntity<Long> {

    @ManyToOne
    @JoinColumn(nullable = false)
    private InvoiceNature invoiceNature;

    @Column(nullable = false)
    private Integer startDay;
    @Column(nullable = false)
    private Integer endDay;
    @Column(nullable = false)
    private Integer notDueDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Organization company;
}
