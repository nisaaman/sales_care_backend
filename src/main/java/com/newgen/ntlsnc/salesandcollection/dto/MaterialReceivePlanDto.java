package com.newgen.ntlsnc.salesandcollection.dto;

import com.newgen.ntlsnc.common.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author sagor
 * @date ১২/৪/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialReceivePlanDto {
    private Long id;

    //@NotNull(message = "Ticket No field is required.")
    // private String ticketNo;
    private Long companyId;

    private String ticketDate;

    @NotNull(message = "Quantity is required.")
    private Float quantity;

    private String requireDate;

    @NotNull(message = "Ticket Status field is required.")
    private String ticketStatus;

    private String ticketStatusDate;

    private String commitmentDate;

    private Long confirmQuantity;

    private String notes;

    //@NotNull(message = "Product field is required.")
    //private Long productId;

    private String itemStatus;

    @NotNull(message = "Sales Booking Details field is required.")
    private Long salesBookingDetailsId;

}
