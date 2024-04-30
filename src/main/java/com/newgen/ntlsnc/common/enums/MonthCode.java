package com.newgen.ntlsnc.common.enums;

/**
 * @author Newaz Sharif
 * @since 20th Aug,22
 */
public enum MonthCode {
    JANUARY("JANUARY", 1),
    FEBRUARY("FEBRUARY", 2),
    MARCH("MARCH", 3),
    APRIL("APRIL", 4),
    MAY("MAY", 5),
    JUNE("JUNE", 6),
    JULY("JULY", 7),
    AUGUST("AUGUST", 8),
    SEPTEMBER("SEPTEMBER", 9),
    OCTOBER("OCTOBER", 10),
    NOVEMBER("NOVEMBER", 11),
    DECEMBER("DECEMBER", 12);

    private String code;
    private Integer name;

    MonthCode(String code, Integer name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public Integer getName() {
        return name;
    }
}
