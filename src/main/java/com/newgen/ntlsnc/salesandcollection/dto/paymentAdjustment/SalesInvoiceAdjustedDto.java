package com.newgen.ntlsnc.salesandcollection.dto.paymentAdjustment;

import com.newgen.ntlsnc.common.enums.CalculationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author anika
 * @Date ১৯/৪/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceAdjustedDto {

    @NotNull(message = "Invoice is required")
    private Long id;
    @NotNull(message = "Invoice number is required")
    private String invoiceNo;
    @NotNull(message = "Invoice date is required")
    private String invoiceDate;
    @NotNull(message = "Invoice amount is required")
    private Float invoiceAmount;
    @NotNull(message = "Remaining amount is required")
    private Float remainingAmount;
    @NotNull(message = "Adjusted amount is required")
    private Float adjustedAmount;

    @NotNull(message = "Elapsed day is required")
    private Integer elapsedDay;
    @NotNull(message = "Ord amount is required")
    private Float ordAmount;
    @NotNull(message = "Invoice nature is required")
    private String invoiceNature;
    @NotNull(message = "Calculation type is required")
    private String calculationType;
    @NotNull(message = "Ord is required")
    private Float ord;

    private String isOpeningBalance;
}
