package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.entity.ProductTradePrice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author kamal
 * @Date ১০/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SalesOrderDetails extends SuperEntity<Long> {

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SalesBookingDetails salesBookingDetails;
}
