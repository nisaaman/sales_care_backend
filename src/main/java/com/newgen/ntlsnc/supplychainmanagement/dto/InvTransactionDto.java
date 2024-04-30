package com.newgen.ntlsnc.supplychainmanagement.dto;

import com.newgen.ntlsnc.common.enums.InvTransactionType;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBookingDetailsDto;
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
public class InvTransactionDto {

    private Long id;

    private String transactionDate;

    @NotNull(message = "Transaction Type field is required.")
    private String transactionType;

    private String storeType;
    private String remarks;

    /*@NotNull(message = "Organization field is required.")*/
    private Long organizationId;

//    @NotNull(message = "Organization field is required.")
//    private Long organizationId;


//    @NotNull(message = "Company field is required.")
    private Long companyId;

    private  List<InvTransactionDetailsDto> invTransactionDetailsDtoList;

    private InvDeliveryChallanDto invDeliveryChallanDto;

}
