package com.newgen.ntlsnc.salesandcollection.dto;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author anika
 * @Date ২২/৬/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributorCompanyMapDto {
    private Long id;

    @NotNull(message = "Distributor Id is required")
    private Long distributorId;

    @NotNull(message = "Organization Id is required")

    private Long companyId;

    private Long organizationId;

}
