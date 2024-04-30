package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Newaz Sharif
 * @since 3rd Oct, 22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterStoreStockMovementDto {

    private Long id;
    private String movementDate;
    private String reason;
    private String note;

    @NotNull(message = "DepotId field is required.")
    private Long depotId;

    @NotNull(message = "CompanyId field is required.")
    private Long companyId;
    private Long organizationId;

    private List<InvTransactionDetailsDto> invTransactionDetailsDtoList;
    private InvTransactionDto invTransactionDto;
}
