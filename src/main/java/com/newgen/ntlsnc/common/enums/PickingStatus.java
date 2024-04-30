package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 4/11/22
 * @time 9:13 AM
 */
public enum PickingStatus {
    PENDING("PENDING", "Pending"),
    CONFIRMED("CONFIRMED", "Confirmed"),
    CANCELLED("CANCELLED", "Cancelled");


    private String code;
    private String name;

    PickingStatus(String code, String name) {
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
