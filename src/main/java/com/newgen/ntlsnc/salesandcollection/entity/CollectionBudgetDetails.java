package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.globalsettings.entity.Location;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author Newaz Sharif
 * @since 26th July,22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CollectionBudgetDetails extends SuperEntity<Long> {
    @Column(nullable = true)
    private Integer quantity;

    @Column(nullable = false)
    private Double productTradePrice;

    private Float manufacturingPrice = 0.0f;
    @Column(nullable = false)
    private Integer month;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Column(nullable = false)
    private Double collectionBudgetAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private CollectionBudget collectionBudget;

    @ManyToOne(fetch = FetchType.LAZY)
    private Distributor distributor;

    @ManyToOne(fetch = FetchType.LAZY)
    private ApplicationUser salesOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

}
