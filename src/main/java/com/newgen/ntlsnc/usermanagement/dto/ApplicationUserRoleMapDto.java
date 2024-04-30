package com.newgen.ntlsnc.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author anika
 * @Date ৫/৬/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUserRoleMapDto {

    private Long id;

    @NotNull(message = "ApplicationUser is required.")
    private Long userId;

    @NotNull(message = "Role is required.")
    private List<Long> roleList;

    private Boolean isActive;

    private Boolean isSave;
}
