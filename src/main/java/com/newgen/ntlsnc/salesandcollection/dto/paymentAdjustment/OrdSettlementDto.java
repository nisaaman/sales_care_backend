package com.newgen.ntlsnc.salesandcollection.dto.paymentAdjustment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author kamal
 * @Date ২১/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdSettlementDto {
    @NotNull(message = "Payment adjustment is required.")
    private Long id;

    @NotNull(message = "Ord amount is required.")
    private Float ordAmount;

    //@NotNull(message = "Sales invoice is required.")
    private Long salesInvoiceId;

    private Long distributorBalanceId;

    @NotNull(message = "Payment collection is required.")
    private Long paymentCollectionId;

    @NotNull(message = "Company is required.")
    private Long companyId;
}
