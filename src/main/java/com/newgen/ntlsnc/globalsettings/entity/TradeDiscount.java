package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.CalculationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author nisa
 * @date 4/7/22
 * @time 11:45 AM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TradeDiscount extends SuperEntity<Long> {

    @Column(nullable = false, length = 100)
    private String   discountName; // Eid Offer

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CalculationType calculationType;

    @Column(nullable = false)
    private Float discountValue = 0.0f;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Semester  semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product  product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvoiceNature  invoiceNature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organization company;

    //todo need to add date for multiple time discount setup in a semester
}
