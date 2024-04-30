package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 4/11/22
 * @time 10:16 AM
 */
public enum SalesBookingStatus {
    SALES_BOOKED("SALES_BOOKED", "Sales Booked"),
    STOCK_CONFIRMED("STOCK_CONFIRMED", "Stock Confirmed"),
    ORDER_CONVERTED("ORDER_CONVERTED", "Order Converted"),
    PARTIAL_ORDER_CONVERTED("PARTIAL_ORDER_CONVERTED", "Partial Order Converted"),
    TICKET_REQUESTED("TICKET_REQUESTED", "Ticket Requested"),
    TICKET_CONFIRMED("TICKET_CONFIRMED", "Ticket Confirmed"),
    TICKET_REJECTED("TICKET_REJECTED", "Ticket Rejected");

    private String code;
    private String name;

    SalesBookingStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
