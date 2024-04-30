package com.newgen.ntlsnc.globalsettings.dto;

import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

/**
 * @author marziah
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeDiscountDto {

    private Long id;

    @NotNull(message = "Discount Name is required.")
    private String discountName;

    @NotNull(message = "Calculation Type is required.")
    private String calculationType;

    @NotNull(message = "Discount Value  is required.")
    private Float discountValue;

    private ApprovalStatus approvalStatus;

    @NotNull(message = "Semester is required.")
    private Long semesterId;

    @NotNull(message = "Product  is required.")
    private Long productId;

    @NotNull(message = "Invoice Nature is required.")
    private Long invoiceNatureId;

    @NotNull(message = "Company field is required.")
    private Long companyId;

}
