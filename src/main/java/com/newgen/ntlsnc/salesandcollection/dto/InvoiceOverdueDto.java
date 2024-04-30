package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author kamal
 * @Date ২৪/৮/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceOverdueDto {
    private Long id;

    @NotNull(message = "Invoice Nature is required")
    private Long invoiceNatureId;

    @NotNull(message = "Start Day is required")
    private Integer startDay;
    @NotNull(message = "End Day is required")
    private Integer endDay;
    @NotNull(message = "Not Due Days is required")
    private Integer notDueDays;

    @NotNull(message = "Company is required")
    private Long companyId;

    private Long previousCompanyId;  // for update; to check if same company or not
}
