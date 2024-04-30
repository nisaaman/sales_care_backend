package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 4/13/22
 * @time 12:05 PM
 */
public enum VehicleType {
    TRUCK("TRUCK", "Truck"),
    PICKUP("PICKUP", "Pickup"),
    VAN("VAN", "Van"),
    N_A("N_A", "N/A");

    private String code;
    private String name;

    VehicleType(String code, String name) {
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
