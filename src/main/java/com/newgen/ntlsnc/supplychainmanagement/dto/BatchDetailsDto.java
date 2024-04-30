package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author kamal
 * @Date ১৩/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDetailsDto {
    private Long id;

    @NotNull(message = "Product is required.")
    private Long productId;

    @NotNull(message = "Expiry Date field is required.")
    private String expiryDate;

    @NotNull(message = "Supervisor field is required.")
    private Long supervisorId;  //ApplicationUser

    private Long organizationId;
}
