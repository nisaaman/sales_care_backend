package com.newgen.ntlsnc.salesandcollection.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author liton
 * Created on 4/24/22 11:27 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class VatSetup extends SuperEntity<Long> {

    @Column(nullable = false)
    private LocalDateTime fromDate;

    private LocalDateTime toDate;

    @Column(nullable = false)
    private Float vat = 0.0f;

    @Column(nullable = false)
    private Boolean vatIncluded;

    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;
}
