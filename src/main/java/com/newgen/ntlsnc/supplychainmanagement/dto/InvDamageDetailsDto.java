package com.newgen.ntlsnc.supplychainmanagement.dto;

import com.newgen.ntlsnc.common.enums.QaStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * @author anika
 * @Date ৩১/৫/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvDamageDetailsDto {

    private Long id;

    private Float quantityInUom = 0.0f;
    private Float war = 0.0f; // Batch wise Stock Valuation (Weighted average rate)
    private String qaDate;

    @NotNull(message = "Quantity field is required.")
    private Integer damageQty;

    private Integer quantity;

    @NotNull(message = "Batch field is required.")
    private Long batchId;

    @NotNull(message = "Product field is required.")
    private Long productId;

    private Long invDamageId;

    private QaStatus qaStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private Long qaById;
}
