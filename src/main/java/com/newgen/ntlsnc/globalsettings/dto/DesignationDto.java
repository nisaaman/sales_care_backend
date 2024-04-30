package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesignationDto {

    private Long id;

    @NotNull(message = "Name field is required.")
    private String name;

    private String description;

    private Boolean isActive;

//    @NotNull(message = "Organization field is required.")
//    private Long organizationId;

}
