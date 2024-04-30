package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 4/13/22
 * @time 11:49 AM
 */
public enum InvItemStatus {
    IN("IN", "In"),
    OUT("OUT", "Out");

    private String code;
    private String name;

    InvItemStatus(String code, String name) {
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
