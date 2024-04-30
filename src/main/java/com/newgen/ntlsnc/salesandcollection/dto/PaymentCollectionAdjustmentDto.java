package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @author kamal
 * @Date ২১/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCollectionAdjustmentDto {
    private Long id;

    @NotNull(message = "Adjusted Amount field is required.")
    private Float adjustedAmount;

    @NotNull(message = "Mapping Date field is required.")
    private String mappingDate;

    @NotNull(message = "Sales Invoice field is required.")
    private Long salesInvoiceId;

    @NotNull(message = "Payment Collection field is required.")
    private Long paymentCollectionId;

    @NotNull(message = "Company field is required.")
    private Long companyId;

    @NotNull(message = "Organization field is required.")
    private Long organizationId;
}
