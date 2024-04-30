package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author kamal
 * @Date ১২/৪/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderDto {
    private Long salesOrderId;

//    @NotNull(message = "order No. field is required.")
//    private String orderNo;

    //@NotNull(message = "Order Date field is required.")
    //private String orderDate;

    @NotNull(message = "Delivery Date field is required.")
    private String deliveryDate;


    @NotNull(message = "Company field is required.")
    private Long companyId;


    @NotNull(message = "Sales Booking field is required.")
    private Long salesBookingId;


    private Long organizationId;

    private List<SalesOrderDetailsDto> salesOrderDetailsDto;

}
