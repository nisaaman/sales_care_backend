package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationTypeDto {

    private Long id;

    @Column(nullable = false, length = 100)
    private String name;  // Zone,

    @Column(nullable = false)
    private Integer level;  //1, 2

    @NotNull(message = "Organization field is required.")
    private Long organizationId;

    @NotNull(message = "Location Tree field is required.")
    private Long locationTreeId;
}
