package com.newgen.ntlsnc.common.enums;

/**
 * @author sunipa
 * @date ১৯/৪/২২
 * @time ৩:৫৯ PM
 */
public enum IntactType {

    MC("MC", "Master Cartoon"),
    IP("IP", "Inner Pack"),
    BU("BU", "Base Unit");

    private String code;
    private String name;

    IntactType(String code, String name) {
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
