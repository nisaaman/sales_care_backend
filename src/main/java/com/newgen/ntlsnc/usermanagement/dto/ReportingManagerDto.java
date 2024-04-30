package com.newgen.ntlsnc.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author anika
 * @Date ২১/৬/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportingManagerDto {
    private Long id;
    private Long applicationUserId;
    private Long reportingToId;
}
