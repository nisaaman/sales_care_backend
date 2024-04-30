package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author anika
 * @Date ১/৬/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBookDto {

    Long id;

    @NotNull(message = "book Number is required.")
    private String bookNumber;

    @NotNull(message = "Issue Date is required.")
    private String issueDate;

    @NotNull(message = "From Mr No is required.")
    private Long fromMrNo;

    private String remarks;

    private Boolean status = true;

    @NotNull(message = "To Mr No is required.")
    private Long toMrNo;

    @NotNull(message = "Company is required.")
    private Long companyId;

    @NotNull(message = "Location is required.")
    private Long paymentBookLocationId;
}
