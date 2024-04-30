package com.newgen.ntlsnc.common.enums;

/**
 * @author Newaz Sharif
 * @since 17th Aug, 22
 */
public enum Month {

    JANUARY("JANUARY", "Jan"),
    FEBRUARY("FEBRUARY", "Feb"),
    MARCH("MARCH", "Mar"),
    APRIL("APRIL", "Apr"),
    MAY("MAY", "May"),
    JUNE("JUNE", "Jun"),
    JULY("JULY", "Jul"),
    AUGUST("AUGUST", "Aug"),
    SEPTEMBER("SEPTEMBER", "Sep"),
    OCTOBER("OCTOBER", "Oct"),
    NOVEMBER("NOVEMBER", "Nov"),
    DECEMBER("DECEMBER", "Dec");

    private String code;
    private String name;

    Month(String code, String name) {
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
