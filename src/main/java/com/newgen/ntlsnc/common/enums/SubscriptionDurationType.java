package com.newgen.ntlsnc.common.enums;

/**
 * @author liton
 * Created on 4/19/22 12:28 PM
 */
public enum  SubscriptionDurationType {
    DAY("DAY", "Days"),
    MONTH("MONTH", "Month"),
    YEAR("YEAR", "Year");

    private String code;
    private String name;

    SubscriptionDurationType(String code, String name) {
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
