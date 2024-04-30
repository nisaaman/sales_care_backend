package com.newgen.ntlsnc.supplychainmanagement.dto;

import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * @author sunipa
 * @Date 22/09/22
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvClaimDto {

    private Long id;
    private String claimDate;
    private String notes;

    @NotNull(message = "Company field is required.")
    private Long companyId;

    private Long organizationId;
    private ApprovalStatus approvalStatus;

    @NotNull(message = "Inventory transaction field is required.")
    private Long invTransactionId;
    private Long invTransferId;

    List<InvClaimDetailsDto> invClaimDetailsDto;
}
