package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author kamal
 * @Date ১১/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesBookingDetailsDto {
    private Long id;

    @NotNull(message = "Quantity field is required.")
    private Integer quantity;

    private Integer freeQuantity;

    private String salesBookingStatus;

    private Long salesBookingId;

    private Long productId;

    private Long productTradePriceId;

    private Long tradeDiscountId;

    private Long organizationId;


}
