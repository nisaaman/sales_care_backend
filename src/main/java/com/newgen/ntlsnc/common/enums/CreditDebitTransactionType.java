package com.newgen.ntlsnc.common.enums;

/**
 * @author sunipa
 * @date 6/30/22
 * @time 12:39 PM
 */
public enum CreditDebitTransactionType {
    SALES_RETURN("SALES_RETURN", "Sales Return"),
    ORD("ORD", "Overriding Discount"),
    DISCOUNT("DISCOUNT", "Discount");

    private String code;
    private String name;

    CreditDebitTransactionType(String code, String name) {
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
