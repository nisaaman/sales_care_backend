package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 4/11/22
 * @time 9:13 AM
 */
public enum TicketStatus {
    REQUESTED("REQUESTED", "Requested"),
    CONFIRMED("CONFIRMED", "Confirmed"),
    REJECTED("REJECTED", "Rejected");

    private String code;
    private String name;

    TicketStatus(String code, String name) {
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
