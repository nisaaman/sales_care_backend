package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * @author marziah
 * @Date 17/04/22
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvTransferDto {
    private Long id;

    //@NotNull(message = "Transfer Date field is required.")
    private String transferDate;

    private String driverName;

    private String driverContactNo;

    private String remarks;

    private String invReturnType;

    private String returnReason;

    private Long invTransferId;

    @NotNull(message = "From DepotId field is required.")
    private Long fromDepotId;

    @NotNull(message = "To DepotId field is required.")
    private Long toDepotId;

    @NotNull(message = "VehicleId field is required.")
    private Long vehicleId;

    //@NotNull(message = "StoreId field is required.")
    private Long storeId;

    //@NotNull(message = "InvTransactionId field is required.")
    private Long invTransactionId;

    @NotNull(message = "CompanyId field is required.")
    private Long companyId;

    //@NotNull(message = "OrganizationId field is required.")
    private Long organizationId;

    private List<InvTransactionDetailsDto> invTransactionDetailsDtoList;

    private InvTransactionDto invTransactionDto;

}
