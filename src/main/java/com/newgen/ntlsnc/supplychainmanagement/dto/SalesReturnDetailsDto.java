package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author nisa
 * @Date 1/21/24 , 2:42 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReturnDetailsDto {

    @NotNull(message = "SalesReturnProposalDetails is required.")
    private Long salesReturnProposalDetailsId;

    @NotNull(message = "Quantity is required.")
    private Float quantity;

    @NotNull(message = "Batch Id is required.")
    private Long batchId;

    @NotNull(message = "SalesInvoice field is required.")
    private Long salesInvoiceId;

    @NotNull(message = "Delivery Challan field is required.")
    private Long deliveryChallanId;

    private boolean isSetDone = false;
}
