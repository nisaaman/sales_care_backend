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
public class InvoiceNatureDto {

    private Long id;

    @NotNull(message = "Name field is required.")
    private String name; // Advance or Cash or Credit

    private String description;

    @NotNull(message = "Organization field is required.")
    private Long organizationId;
}
