package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author kamal
 * @Date ২৫/৫/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationTreeDto {

    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 250)
    private String description;

    @NotNull(message = "Organization field is required.")
    private Long organizationId;

    @NotNull(message = "List of Location Type are required.")
    private List<LocationTypeDto> locationTypeList;

    @NotNull(message = "Location Tree is required.")
    private List<Map> locationTree;
}
