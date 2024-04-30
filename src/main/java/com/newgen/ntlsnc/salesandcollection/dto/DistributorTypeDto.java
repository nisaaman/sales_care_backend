package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author anika
 * @Date ২৫/৫/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributorTypeDto {

    private Long id;

    @NotNull(message = "Distributor Type Name is required")
    private  String name;


}
