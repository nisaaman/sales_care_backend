package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 9/10/22
 * @time 4:00 PM
 */

public enum ApprovalFeature {
    SALES_BOOKING("SALES_BOOKING", "Sales Booking"),
    SALES_RETURN("SALES_RETURN", "Sales Return"),
    CREDIT_LIMIT("CREDIT_LIMIT", "Credit Limit"),
    SALES_BUDGET("SALES_BUDGET", "Sales Budget"),
    COLLECTION_BUDGET("COLLECTION_BUDGET", "Collection Budget"),
    CREDIT_DEBIT_NOTE("CREDIT_DEBIT_NOTE", "Credit/Debit Note"),
    DAMAGE_DECLARATION("DAMAGE_DECLARATION", "Damage Declaration"),
    SALES_ORDER("SALES_ORDER", "Sales Order"),

    ADVANCE_PAYMENT_COLLECTION("ADVANCE_PAYMENT_COLLECTION", "Advance Payment Collection");

    private String code;
    private String name;
    ApprovalFeature(String code, String name) {
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
