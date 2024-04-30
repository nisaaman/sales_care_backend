package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

/**
 * @author kamal
 * @Date ১৭/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvTransactionDetailsDto {
    private Long id;

    @NotNull(message = "Quantity field is required.")
    private Float quantity;

    //private Float quantityInUom;

    private Float rate; // Batch wise Stock Valuation

//    @NotNull(message = "Inventory Item Status field is required.")
//    private String invItemStatus;

    @NotNull(message = "Batch field is required.")
    private Long batchId;//

    //@NotNull(message = "From Store field is required.")
    private Long fromStoreId;

    @NotNull(message = "To Store field is required.")
    private Long toStoreId;

    @NotNull(message = "Product field is required.")
    private Long productId;

    //@NotNull(message = "Inventory Transaction field is required.")
    //private Long invTransactionId;

    //@NotNull(message = "Organization field is required.")
    //private Long organizationId;

    @Enumerated(EnumType.STRING)
    private String qaStatus; //if receive in quarantine

    private String qaDate; //if receive in quarantine

    private Long qaById; //if receive in quarantine

    private Long pickingId;

    private Long salesOrderDetailsId;

    private Long pickingDetailsId;

}
