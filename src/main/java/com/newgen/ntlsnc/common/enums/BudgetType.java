package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 4/9/22
 * @time 1:39 PM
 */
public enum BudgetType {
    SALES_OFFICER("SALES_OFFICER", "Sales Officer"),
    DISTRIBUTOR("DISTRIBUTOR", "Distributor"),
    LOCATION("LOCATION", "Location");

    private String code;
    private String name;

    BudgetType(String code, String name) {
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
