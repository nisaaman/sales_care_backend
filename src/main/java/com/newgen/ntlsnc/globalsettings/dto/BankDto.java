package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author kamal
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankDto {

    private Long id;

    @NotNull(message = "Name field is required.")
    private String name;

    @NotNull(message = "Short name field is required.")
    private String bankShortName;

    private String description;

    private Boolean isActive;
}
