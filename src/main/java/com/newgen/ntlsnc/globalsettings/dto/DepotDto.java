package com.newgen.ntlsnc.globalsettings.dto;

import com.newgen.ntlsnc.globalsettings.entity.DepotQualityAssuranceMap;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorSalesOfficerMapDto;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepotDto {

    private Long id;

    @Size(max=20, message = "Length is not more than 20")
    private String code;

    @NotNull(message = "Depot Name field is required.")
    @Size(max = 80, message = "Length is not more than 80")
    private String depotName;

    @NotNull(message = "Central Warehouse field is required.")
    private Boolean isCentralWarehouse;

    private String address;

    @NotNull(message = "Contact Number field is required.")
    @Size(max = 11)
    private String contactNumber;

    @NotNull(message = "Depot Manager field is required.")
    private Long depotManagerId;

    private Boolean isActive;

    @NotNull(message = "Company and Area List is required.")
    private List<Map<String, Object>> depotCompanyLocationList;

    private List<DepotQualityAssuranceMapDto> depotQualityAssuranceMapDtoList;
}
