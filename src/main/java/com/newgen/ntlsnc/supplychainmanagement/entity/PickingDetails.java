package com.newgen.ntlsnc.supplychainmanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.InvItemStatus;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.entity.Store;
import com.newgen.ntlsnc.salesandcollection.entity.SalesOrder;
import com.newgen.ntlsnc.salesandcollection.entity.SalesOrderDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 4/16/22
 * @time 12:57 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PickingDetails extends SuperEntity<Long> {

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Picking picking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SalesOrderDetails salesOrderDetails;

//    @ManyToOne(fetch = FetchType.LAZY)
//    private InvTransactionDetails invTransactionDetails;

    private Integer goodQty;

    private Integer badQty;

    private String reason;
}
