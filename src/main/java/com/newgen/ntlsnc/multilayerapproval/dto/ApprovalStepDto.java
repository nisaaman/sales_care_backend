package com.newgen.ntlsnc.multilayerapproval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author nisa
 * @date 9/11/22
 * @time 10:20 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStepDto {
    private Long id;
    @NotNull
    private String name;
    private String description;
}
