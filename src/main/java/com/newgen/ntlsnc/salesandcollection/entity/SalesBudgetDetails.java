package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.entity.ProductTradePrice;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 4/9/22
 * @time 11:06 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SalesBudgetDetails extends SuperEntity<Long> {

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double productTradePrice;

    private Float manufacturingPrice = 0.0f;
    @Column(nullable = false)
    private Integer month;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesBudget salesBudget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distributor_id")
    private Distributor distributor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_officer_id")
    private ApplicationUser salesOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;
}
