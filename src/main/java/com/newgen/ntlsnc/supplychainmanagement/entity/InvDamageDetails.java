package com.newgen.ntlsnc.supplychainmanagement.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.QaStatus;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author nisa
 * @date 5/18/22
 * @time 6:07 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InvDamageDetails extends SuperEntity<Long> {

    @Column(nullable = false)
    private Integer quantity;

    private Float quantityInUom = 0.0f;

    private Float rate = 0.0f; // Batch wise Stock Valuation

    @ManyToOne
    @JoinColumn(nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvDamage invDamage;

    /*@Enumerated(EnumType.STRING)
    private QaStatus qaStatus;

    private LocalDateTime qaDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private ApplicationUser qaBy;*/

    // QA Report upload
}
