package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author anika
 * @Date ২০/৬/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepotCompanyMappingDto {

    private Long id;

    @NotNull(message = "Depot Name field is required.")
    private Long depotId;

    @NotNull(message = "Company Name field is required.")
    private Long companyId;

}
