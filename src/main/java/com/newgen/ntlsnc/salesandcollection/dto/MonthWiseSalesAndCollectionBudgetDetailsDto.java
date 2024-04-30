package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author anika
 * @Date ১৩/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthWiseSalesAndCollectionBudgetDetailsDto {

    private Long id;
    @NotNull(message = "Quantity field is required.")
    private Double quantity;

    @NotNull(message = "Product Trade Price field is required.")
    private Long productTradePriceId;

    @NotNull(message = "Product field is required.")
    private Long productId;

    @NotNull(message = "Month Wise Sales And Collection Budget field is required.")
    private Long monthWiseSalesAndCollectionBudgetId;


}
