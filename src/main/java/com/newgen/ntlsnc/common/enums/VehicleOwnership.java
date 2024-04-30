package com.newgen.ntlsnc.common.enums;

public enum VehicleOwnership {

    RENTAL("RENTAL", "rental"),
    OWNED("OWNED", "owned");

    private String code;
    private String name;

    VehicleOwnership(String code, String name) {
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
