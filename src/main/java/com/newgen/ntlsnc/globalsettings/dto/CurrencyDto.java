package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author kamal
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDto {

    private Long id;

    @Size(max = 20, message = "Name length cannot be more than 20 character")
    @NotNull(message = "Name field is required.")
    private String name;

    @Size(max = 80, message = "Description length cannot be more than 80 character")
    private String description;

    private Boolean isActive;

//    @NotNull(message = "Organization field is required.")
//    private Long organizationId;
}
