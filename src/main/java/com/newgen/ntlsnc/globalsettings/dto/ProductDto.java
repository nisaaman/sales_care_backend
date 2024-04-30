package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author kamal
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;

    private String productSku;

    private String name;

    @NotNull(message = "Item Size is required.")
    private Integer itemSize;

    private Long packSizeId;

    @NotNull(message = "Expiry Days is required.")
    private Integer expiryDays;

    @NotNull(message = "Minimum Stock is required.")
    private Integer minimumStock;

    @NotNull(message = "Product Category Id is required.")
    private Long productCategoryId;

    private Long uomId;

    @NotNull(message = "Company field is required.")
    private Long companyId;

    private String customUOMName;
    private String customUOMDescription;
}
