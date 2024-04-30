package com.newgen.ntlsnc.common.enums;

/**
 * @author kamal
 * @Date ২/১১/২২
 */
public enum CreditLimitTerm {
    LT("LONG_TERM", "Long Term"),
    ST("SHORT_TERM", "Short Term"),
    SB("SALES_BOOKING", "Sales Booking");

    private String code;
    private String name;

    CreditLimitTerm(String code, String name) {
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
