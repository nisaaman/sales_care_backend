package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 4/16/22
 * @time 12:39 PM
 */
public enum InvReturnType {
    SHORT("SHORT", "Short"),
    MISSING("MISSING", "Missing"),
    EXCESS("EXCESS", "Excess");
//    DAMAGE("DAMAGE", "Damage");

    private String code;
    private String name;

    InvReturnType(String code, String name) {
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
