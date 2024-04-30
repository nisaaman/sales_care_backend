package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 4/13/22
 * @time 11:10 AM
 */
public enum StoreType {
    REGULAR("REGULAR", "Regular"),
    RESTRICTED("RESTRICTED", "Restricted"),
    QUARANTINE("QUARANTINE", "Quarantine"),
    IN_TRANSIT("IN_TRANSIT", "In Transit");

    private String code;
    private String name;

    StoreType(String code, String name) {
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
