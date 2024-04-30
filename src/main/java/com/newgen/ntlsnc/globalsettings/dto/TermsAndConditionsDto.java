package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author kamal
 * @Date ১৯/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermsAndConditionsDto {
    private Long id;

    @NotNull(message = "Terms And Conditions field is required.")
    private String termsAndConditions;

    @NotNull(message = "Company field is required.")
    private Long companyId;
}
