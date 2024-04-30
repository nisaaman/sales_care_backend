package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author nisa
 * @date 4/5/22
 * @time 1:18 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product extends SuperEntity<Long> {
    private String productSku; // AE + 001
    private String name; // System Plus

    @Column(nullable = false)
    private Integer itemSize; // 200

    @ManyToOne(fetch = FetchType.LAZY)
    private PackSize packSize; // 1

    @Column(nullable = false)
    private Integer expiryDays;
    @Column(nullable = false)
    private Integer minimumStock; // pcs

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductCategory productCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    private UnitOfMeasurement uom; // ltr

    @ManyToOne(fetch = FetchType.LAZY)
    private Organization company;
}
