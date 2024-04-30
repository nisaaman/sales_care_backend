package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasurementDto {

    private Long id;

    @NotNull(message = "Abbreviation field is required.")
    private String abbreviation;

    private String description;

    private Boolean isActive;

}
