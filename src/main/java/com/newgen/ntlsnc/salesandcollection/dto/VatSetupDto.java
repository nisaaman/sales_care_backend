package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author anika
 * @Date ২৪/৪/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VatSetupDto {

    private Long id;

    @NotNull(message = "From Date is required")
    private String fromDate;

    @NotNull(message = "To Date is required")
    private String toDate;

    @NotNull(message = "VAT is required")
    private Float vat = 0.0f;

    @NotNull(message = "VAT Included is required")
    private Boolean vatIncluded;

    private String remarks;

    @NotNull(message = "Product is required")
    private Long productId;
}
