package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author kamal
 * @Date ১৬/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDto {
    private Long id;

    @NotNull(message = "Name field is required.")
    private String name;

    @NotNull(message = "Short Name field is required.")
    private String shortName;

    @NotNull(message = "Store Type field is required.")
    private String storeType;

    private String description;

    private Boolean isActive;
}
