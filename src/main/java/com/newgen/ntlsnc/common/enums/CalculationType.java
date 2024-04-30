package com.newgen.ntlsnc.common.enums;

/**
 * @author kamal
 * @Date ৩/৭/২২
 */
public enum CalculationType {
    PERCENTAGE("PERCENTAGE", "%"),
    EQUAL("EQUAL", "=");

    private String code;
    private String name;

    CalculationType(String code, String name) {
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
