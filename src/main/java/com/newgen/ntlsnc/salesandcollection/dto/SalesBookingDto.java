package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author kamal
 * @Date ১১/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesBookingDto {

    private Long id;

    private String bookingNo;

    private String bookingDate;

    private String tentativeDeliveryDate;

    private String notes;

    private String approvalStatus;

    private String approvalDate;

    private Boolean isHold;

    private String holdReason;

    private Long invoiceNatureId;

    private Long distributorId;

    private Long semesterId;

    private Long salesOfficerId;

    private Long depotId; //warehouse

    private Long companyId;

    private Long organizationId;

    private List<SalesBookingDetailsDto> salesBookingDetailsList;

    private Boolean isFinalSubmit;
}
