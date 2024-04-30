package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 4/10/22
 * @time 3:18 PM
 */
public enum ApprovalStatus {
    DRAFT("DRAFT", "Draft"),
    PENDING("PENDING", "Pending"),
    AUTHORIZATION_FLOW("AUTHORIZATION_FLOW", "Authorization Flow"),
    APPROVED("APPROVED", "Approved"),
    REJECTED("REJECTED", "Rejected");

    private String code;
    private String name;

    ApprovalStatus(String code, String name) {
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
