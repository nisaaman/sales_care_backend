package com.newgen.ntlsnc.supplychainmanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.QaStatus;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.entity.Store;
import com.newgen.ntlsnc.salesandcollection.entity.SalesOrderDetails;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author nisa
 * @date 4/13/22
 * @time 11:07 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InvTransactionDetails extends SuperEntity<Long> {

    @Column(nullable = false)
    private Float quantity;

    private Float quantityInUom = 0.0f;

    private Float rate = 0.0f; // Batch wise Stock Valuation

    @ManyToOne
    @JoinColumn(nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    private Store fromStore;

    @ManyToOne(fetch = FetchType.LAZY)
    private Store toStore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvTransaction invTransaction;

    @Enumerated(EnumType.STRING)
    private QaStatus qaStatus; //if receive in quarantine

    private LocalDateTime qaDate; //if receive in quarantine

    @ManyToOne(fetch = FetchType.LAZY)
    private ApplicationUser qaBy; //if receive in quarantine

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesOrderDetails salesOrderDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    private PickingDetails pickingDetails;

    //    TODO: Sales Return QA report will be included
}
