package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.IntactType;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.entity.ProductTradePrice;
import com.newgen.ntlsnc.globalsettings.entity.TradeDiscount;
import com.newgen.ntlsnc.supplychainmanagement.entity.Batch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author marziah
 * @Date 13/04/22
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SalesReturnProposalDetails extends SuperEntity<Long> {

    @Column(nullable = false)
    private Float quantity;   //save as IntactType.IP

    private Float rate; // Batch wise

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private IntactType intactType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ProductTradePrice productTradePrice; // id

    @ManyToOne(fetch = FetchType.LAZY)
    private TradeDiscount tradeDiscount; //added for product trade price with discount

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SalesReturnProposal salesReturnProposal;
}
