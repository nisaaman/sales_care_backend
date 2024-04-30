package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author anika
 * @Date ২/৬/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationManagerMapDto {

    private Long id;
    private String fromDate;
    private String toDate;
    private Long locationId;
    private Long applicationUserId;
    private Long companyId;
    private Long locationTypeId;
}
