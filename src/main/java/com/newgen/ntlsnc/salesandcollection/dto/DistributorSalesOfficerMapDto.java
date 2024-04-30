package com.newgen.ntlsnc.salesandcollection.dto;

import com.newgen.ntlsnc.globalsettings.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author anika
 * @Date ২১/৬/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributorSalesOfficerMapDto {

    private Long id;

    private String fromDate;
    private String toDate;
    private Long salesOfficerId;
//    private Long distributorId;
//    private Long organizationId;
    private Long companyId;
    private Long locationId;

    private List<Location> locationArray;

    private List<Map<String, Object>> soArray;


}

