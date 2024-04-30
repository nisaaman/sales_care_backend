package com.newgen.ntlsnc.usermanagement.dto;

import com.newgen.ntlsnc.globalsettings.dto.OrganizationDto;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author anika
 * @Date ২০/৬/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUserCompanyMappingDto {

    private Long id;

    @NotNull(message = "Application User is required.")
    private Long userId;

    @NotNull(message = "Company is required.")
    private List<Organization> companyList;

}
