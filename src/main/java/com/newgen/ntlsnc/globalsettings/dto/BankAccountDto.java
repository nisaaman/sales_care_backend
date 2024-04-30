package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDto {
    private Long id;

    @NotNull(message = "Account Number field is required.")
    private String accountNumber;

    @NotNull(message = "Bank branch field is required.")
    private Long bankBranchId;

    private Boolean isActive;

//    @NotNull(message = "Organization field is required.")
//    private Long organizationId;
}
