package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 5/26/22
 * @time 4:57 PM
 */
public enum NoteType {
    DEBIT("DEBIT", "Debit"),
    CREDIT("CREDIT", "Credit");

    private String code;
    private String name;

    NoteType(String code, String name) {
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
