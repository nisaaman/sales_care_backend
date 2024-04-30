package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackSizeDto {

    private Long id;

    @NotNull(message = "Pack Size field is required.")
    private Integer packSize;

    @NotNull(message = "Height field is required.")
    private Float height;

    @NotNull(message = "Width field is required.")
    private Float width;

    @NotNull(message = "Length field is required.")
    private Float length;

    private String description;

    @NotNull(message = "Unit Of Measurement field is required.")
    private Long uomId;

    private Boolean isActive;

}
