package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.SalesBookingStatus;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.entity.ProductTradePrice;
import com.newgen.ntlsnc.globalsettings.entity.TradeDiscount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author kamal
 * @Date ৯/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SalesBookingDetails extends SuperEntity<Long> {
    @Column(nullable = false)
    private Integer quantity;

    private Integer freeQuantity = 0;

    @Column(length = 40)
    @Enumerated(EnumType.STRING)
    private SalesBookingStatus salesBookingStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SalesBooking salesBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product; // id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ProductTradePrice productTradePrice; // id

    @ManyToOne(fetch = FetchType.LAZY)
    private TradeDiscount tradeDiscount; //added for product trade price with discount
}
