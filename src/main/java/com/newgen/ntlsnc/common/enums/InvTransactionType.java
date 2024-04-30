package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 4/13/22
 * @time 11:39 AM
 */
public enum InvTransactionType {
    PRODUCTION_RECEIVE("PRODUCTION_RECEIVE", "Production Receive"),
    DELIVERY_CHALLAN("DELIVERY_CHALLAN", "Delivery Challan"),
    TRANSFER_SENT("TRANSFER_SENT", "Transfer Sent"),
    TRANSFER_RECEIVE("TRANSFER_RECEIVE", "Transfer Receive"),
    RETURN("RETURN", "Return"), // TODO Need to discussion about feature wise transaction type
    INTER_STORE_MOVEMENT("INTER_STORE_MOVEMENT", "Inter Store Stock Movement"),
    DAMAGE("DAMAGE",  "Damage");

    private String code;
    private String name;

    InvTransactionType(String code, String name) {
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
