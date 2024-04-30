package com.newgen.ntlsnc.supplychainmanagement.dto;

import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author anika
 * @Date ১৭/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvDeliveryChallanDto {

    private Long id;

    @NotNull(message = "Delivery Date is required.")
    private String deliveryDate;//parse
    private String vatChallanNo;
    private String driverName;
    private String driverContactNo;
    private String remarks;

    @NotNull(message = "Vehicle  is required.")
    private Long vehicleId;

    @NotNull(message = "Depot is required.")
    private Long depotId;

    @NotNull(message = "Company is required.")
    private Long companyId;

    @NotNull(message = "Inventory Transaction is required.")
    private Long invTransactionId;

    @NotNull(message = "Organization is required.")
    private Long organizationId;

    private List<InvTransactionDetailsDto> invTransactionDetailsDtoList;

    private InvTransactionDto invTransactionDto;

    @NotNull(message = "Distributor is required")
    private Long distributorId;

    private String customVehicleNo;
}
