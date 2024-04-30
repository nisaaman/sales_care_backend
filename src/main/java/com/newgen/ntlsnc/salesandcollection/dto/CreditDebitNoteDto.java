package com.newgen.ntlsnc.salesandcollection.dto;

import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author anika
 * @Date ৩০/৫/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditDebitNoteDto {

    private Long id;

    @NotNull(message = "Proposal Date is required")
    private String proposalDate;

    @NotNull(message = "Amount is required")
    private Double amount = 0.0D;

    @NotNull(message = "Note Type is required")
    private String noteType;

    @NotNull(message = "Transaction Type is required")
    private String transactionType;

    private ApprovalStatus approvalStatus;

    @NotNull(message = "Reason is required")
    private String reason;

    private String note;

    private Long invoiceId;

    @NotNull(message = "DistributorId is required")
    private Long distributorId;

    @NotNull(message = "CompanyId is required")
    private Long companyId;

    private String isOpeningBalance;

}
