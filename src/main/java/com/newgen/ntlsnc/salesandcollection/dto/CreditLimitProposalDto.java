package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author marziah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditLimitProposalDto {
    private Long id;

    @NotNull(message = "Proposed Amount field is required.")
    private Float proposedAmount;

    private String proposalDate;

    private String proposalNotes;

    private String startDate;

    private String endDate;

    @NotNull(message = "Credit Limit Term is required")
    private String creditLimitTerm;

    @NotNull(message = "Distributor field is required.")
    private Long distributorId;

    @NotNull(message = "Company field is required.")
    private Long companyId;

    private Long salesBookingId;

    private Double businessPlan;
    private Double currentLimit;
}
