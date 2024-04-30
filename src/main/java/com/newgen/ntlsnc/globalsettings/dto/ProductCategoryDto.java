package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author sagor
 * @date ৬/৪/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDto {

    private Long id;

    @NotNull(message = "Name field is required.")
    private String  name;

//    @NotNull(message = "Prefix field is required.")
    private String  prefix; //  AE

//    @NotNull(message = "Category Type field is required.")
    private Long productCategoryTypeId;

    private Long parentId;

    private  String treeLevel;

    @NotNull(message = "Organization field is required.")
    private Long organizationId;

    @NotNull(message = "Company field is required.")
    private Long companyId;

    private List<ProductCategoryDto> childProductCategoryDtoList;

    private List<ProductCategoryDto> children;
}
