package com.newgen.ntlsnc.supplychainmanagement.dto;

import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author anika
 * @Date ৩১/৫/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvDamageDto {

    private Long id;

    private String declarationDate;

    @NotNull(message = "Notes  field is required.")
    private String reason;

    private String notes;

    private Long depotId;

    private Long storeId;

    private String storeType;

    @NotNull(message = "Company  field is required.")
    private Long companyId;

    private ApprovalStatus approvalStatus;

    private List<InvDamageDetailsDto> invDamageDetailsDtoList;

    private List<InvDamageDetailsDto> damageDetailsList;
}
