package com.newgen.ntlsnc.salesandcollection.dto;

import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditLimitDto {

    private Long id;

    @NotNull(message = "Credit Limit is required")
    private Float creditLimit = 0.0f;

    private String startDate;

    private String endDate;

    @NotNull(message = "Distributor is required")
    private Long distributorId;

    @NotNull(message = "Credit Limit Term is required")
    private String creditLimitTerm;

    private Long creditLimitProposalId;

    private Long salesBookingId;

    @NotNull(message = "Company is required")
    private Long companyId;
}
