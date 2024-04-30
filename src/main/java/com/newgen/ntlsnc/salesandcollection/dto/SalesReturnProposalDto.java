package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author marziah
 * @Date 20/04/22
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReturnProposalDto {
    private Long id;

    private String proposalDate;

    private String returnReason;

    @NotNull(message = "Approval Status field is required.")
    private String approvalStatus;

    @NotNull(message = "Distributor field is required.")
    private Long distributorId;


    @NotNull(message = "SalesInvoice field is required.")
    private Long salesInvoiceId;


    @NotNull(message = "Company field is required.")
    private Long companyId;

    @NotNull(message = "Delivery Challan field is required.")
    private Long deliveryChallanId;

    @NotNull(message = "Organization field is required.")
    private Long organizationId;

    private String invoiceFromDate;
    private String invoiceToDate;

    private List<SalesReturnProposalDetailsDto> salesReturnProposalDetailsDtoList;

}
