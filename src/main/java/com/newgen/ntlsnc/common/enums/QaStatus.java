package com.newgen.ntlsnc.common.enums;

/**
 * @author liton
 * Created on 4/24/22 1:11 PM
 */
public enum QaStatus {
    IN_PROGRESS("IN_PROGRESS", "In Progress"),
    PASS("PASS", "Pass"),
    FAILED("FAILED", "Failed");

    private String code;
    private String name;

    QaStatus(String code, String name) {
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
