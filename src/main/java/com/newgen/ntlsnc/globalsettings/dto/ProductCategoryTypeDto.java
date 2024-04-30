package com.newgen.ntlsnc.globalsettings.dto;


import com.newgen.ntlsnc.globalsettings.entity.ProductCategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author marzia
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryTypeDto {

    private Long id;

    @NotNull(message = "Name field is required.")
    private String name;

    @NotNull(message = "Level field is required.")
    private Integer level;

    @NotNull(message = "Organization field is required.")
    private Long organizationId;

    @NotNull(message = "Company field is required.")
    private Long companyId;

    private List<ProductCategoryTypeDto> productCategoryTypeDtoList;

    public ProductCategoryTypeDto(ProductCategoryType productCategoryType){
        this.setId(productCategoryType.getId());
        this.setName(productCategoryType.getName());
        this.setLevel(productCategoryType.getLevel());
        this.setOrganizationId(productCategoryType.getOrganization() == null ? null : productCategoryType.getOrganization().getId());
        this.setCompanyId(productCategoryType.getCompany() == null ? null : productCategoryType.getCompany().getId());
    }
}
