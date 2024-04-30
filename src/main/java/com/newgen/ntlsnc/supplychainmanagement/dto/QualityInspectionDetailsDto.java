package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author Newaz Sharif
 * @since 30th Oct, 22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualityInspectionDetailsDto {

    @NotNull(message = "Batch is Required")
    private Long batchId;
    @NotNull(message = "Product is Required")
    private Long productId;
    @NotNull(message = "QA Status is Required")
    private String status;
    @NotNull(message = "Quantity is Required")
    private Integer quantity;
}
