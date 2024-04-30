package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author kamal
 * @Date ১৮/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickingDetailsDto {
    private Long id;

    @NotNull(message = "Quantity field is required.")
    private Integer quantity;

    @NotNull(message = "Product field is required.")
    private Long productId;

    @NotNull(message = "Sales Order field is required.")
    private Long salesOrderId;

    @NotNull(message = "Sales Order Details field is required.")
    private Long salesOrderDetailsId;
}
