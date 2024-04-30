package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author kamal
 * @Date ১২/৪/২২
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderDetailsDto {
    private Long salesOrderDetailsId;

    @NotNull(message = "Quantity field is required.")
    private Integer orderQuantity;
    private Integer bookingQuantity;
    private Integer remainingBookingQuantity;

    //@NotNull(message = "Sales Order field is required.")
    private Long salesOrderId;


    @NotNull(message = "Product field is required.")
    private Long productId;

    @NotNull(message = "Product Trade Price field is required.")
    private Long productTradePriceId;
    private Long tradeDiscountId;

    @NotNull(message = "Sales Booking Details field is required.")
    private Long salesBookingDetailsId;

    private Long organizationId;
}
