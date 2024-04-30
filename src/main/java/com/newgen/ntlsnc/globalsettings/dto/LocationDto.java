package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    private Long id;

    @NotNull(message = "Name field is required.")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 250)
    private String remarks;

    @NotNull(message = "Location Type field is required.")
    private Long locationTypeId;

    @NotNull(message = "Organization field is required.")
    private Long organizationId;

    private Long parentId;
}
