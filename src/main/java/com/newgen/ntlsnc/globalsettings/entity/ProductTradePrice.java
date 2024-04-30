package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author nisa
 * @date 4/7/22
 * @time 12:54 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ProductTradePrice extends SuperEntity<Long> {

    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Float tradePrice = 0.0f;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product  product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Currency currency;
}
