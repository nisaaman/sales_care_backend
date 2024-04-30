package com.newgen.ntlsnc.multilayerapproval.dto;

import com.newgen.ntlsnc.common.enums.ApprovalFeature;
import com.newgen.ntlsnc.globalsettings.dto.ProductCategoryTypeDto;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.multilayerapproval.entity.ApprovalStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * @author nisa
 * @date 9/11/22
 * @time 10:49 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStepFeatureMapDto {

    private Long id;
    @NotNull(message = "Approval Step is required.")
    private Long approvalStepId;
    @NotNull(message = "Approval Feature is required.")
    private String approvalFeature;

    private Integer level;
    @NotNull(message = "Company is required.")
    private Long companyId;

    private List<ApprovalStepFeatureMapDto> approvalStepFeatureMapDtoList;
}
