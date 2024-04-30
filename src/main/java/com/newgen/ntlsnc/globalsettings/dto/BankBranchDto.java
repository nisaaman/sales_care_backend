package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankBranchDto {
    private Long id;

    @NotNull(message = "Name field is required.")
    private String name;

    @NotNull(message = "Contact number field is required.")
    private String contactNumber;

    private String email;

    private String address;

    @NotNull(message = "Bank field is required.")
    private Long bankId;

    private Boolean isActive;

//    @NotNull(message = "Organization field is required.")
//    private Long organizationId;
}
