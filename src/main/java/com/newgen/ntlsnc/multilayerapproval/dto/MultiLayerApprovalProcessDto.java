package com.newgen.ntlsnc.multilayerapproval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

/**
 * @author nisa
 * @date 9/16/22
 * @time 4:47 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiLayerApprovalProcessDto {

    private Long id;

    @NotNull(message = "Company is required.")
    private Long companyId;

    @NotNull(message = "Multi Layer Approval Path is required.")
    private Long multiLayerApprovalPathId;

    private String approvalFeature;

    private String approvalStatus;

    private String refTable;

    @NotNull(message = "Ref Table is required.")
    private Long refId;

    private String comments;

    @NotNull(message = "Approval Actor is required.")
    private String approvalActor;

    private Long approvalActorId;

    private Long approvalStepId;

    private Integer level;

}
