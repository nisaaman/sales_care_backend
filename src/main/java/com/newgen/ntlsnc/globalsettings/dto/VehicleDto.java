package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author marzia
 * Created on 16/4/22
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {

    private Long id;

    @NotNull(message = "Registration No field is required.")
    private String registrationNo;

    @NotNull(message = "Vehicle Height field is required.")
    private Float vehicleHeight;

    @NotNull(message = "Vehicle Width field is required.")
    private Float vehicleWidth;

    @NotNull(message = "Vehicle Depth field is required.")
    private Float vehicleDepth;

    @NotNull(message = "Vehicle Type field is required.")
    private String vehicleType;

    private String vehicleOwnership;

    private Boolean isActive;

//    @NotNull(message = "Organization field is required.")
//    private Long organizationId;

}
