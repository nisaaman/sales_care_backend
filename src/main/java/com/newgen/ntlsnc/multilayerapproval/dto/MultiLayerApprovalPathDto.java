package com.newgen.ntlsnc.multilayerapproval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author sagor
 * @date 9/12/22
 * @time 12:40 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiLayerApprovalPathDto {

    private Long id;

    @NotNull(message = "Company is required.")
    private Long companyId;

    @NotNull(message = "Approval Step Feature is required.")
    private Long approvalStepFeatureMapId;

    @NotNull(message = "Approval Actor is required.")
    private String approvalActor;

    private Long approvalActorId;
}
