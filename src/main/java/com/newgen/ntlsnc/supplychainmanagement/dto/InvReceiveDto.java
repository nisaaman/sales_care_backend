package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author kamal
 * @Date ১৭/৪/২২
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvReceiveDto {
    private Long id;

    //@NotNull(message = "Receive Date field is required.")
    //private String receiveDate;

    private String remarks;

    @NotNull(message = "Depot field is required.")
    private Long depotId;

    @NotNull(message = "Company field is required.")
    private Long companyId;

    //@NotNull(message = "Organization field is required.")
    //private Long organizationId;

    private Long invTransactionId;  // for update and delete

    private InvTransactionDto invTransactionDto;
    List<InvTransactionDetailsDto> invTransactionDetails;
}
