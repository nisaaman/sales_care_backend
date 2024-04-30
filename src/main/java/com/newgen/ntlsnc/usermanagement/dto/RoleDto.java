package com.newgen.ntlsnc.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author anika
 * @Date ৫/৬/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {

    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
}
