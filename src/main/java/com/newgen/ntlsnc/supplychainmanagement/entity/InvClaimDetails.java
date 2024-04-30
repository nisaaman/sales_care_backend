package com.newgen.ntlsnc.supplychainmanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.InvReturnType;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author sunipa
 * @date ২১/৯/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InvClaimDetails extends SuperEntity<Long> {
    @Column(nullable = false)
    private Integer quantity;
    private Float quantityInUom = 0.0f;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Batch batch;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private InvReturnType claimType;

    private String claimReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvClaim invClaim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvTransactionDetails invTransactionDetails;

}
