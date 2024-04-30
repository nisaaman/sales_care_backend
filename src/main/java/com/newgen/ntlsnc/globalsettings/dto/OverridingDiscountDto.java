package com.newgen.ntlsnc.globalsettings.dto;

import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author sagor
 * @date ৯/৪/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverridingDiscountDto {
    private Long id;

    @NotNull(message = "From Day field is required.")
    private Integer fromDay;
    @NotNull(message = "To Day field is required.")
    private Integer toDay;

    private String calculationType;

    @NotNull(message = "ord field is required.")
    private Double ord;

    private ApprovalStatus approvalStatus;

    private Long semesterId;

    private Long invoiceNatureId;

    private Long companyId;

    private Long organizationId;

    private String previousCompanyIdSemesterIdInvoiceNatureId;   //in update, previous list will be deleted  format: 1-2-3
}
