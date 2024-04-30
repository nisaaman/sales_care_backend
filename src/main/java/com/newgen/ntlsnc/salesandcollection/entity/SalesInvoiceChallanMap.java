package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDeliveryChallan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 5/17/22
 * @time 4:41 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SalesInvoiceChallanMap extends SuperEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SalesInvoice salesInvoice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvDeliveryChallan invDeliveryChallan;
}
