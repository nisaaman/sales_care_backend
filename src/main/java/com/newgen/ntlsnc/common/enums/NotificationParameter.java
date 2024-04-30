package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @Date ২৯/৮/২৩ , ৩:৩৫ PM
 */
public enum NotificationParameter {
    SOUND("default"),
    COLOR("#2D9CDB");

    private String value;

    NotificationParameter(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
