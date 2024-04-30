package com.newgen.ntlsnc.common.enums;

public enum DistributorOpeningBalanceTemplate {
    DISTRIBUTOR_ID("DISTRIBUTOR_ID", "Distributor_Id"),
    DISTRIBUTOR_NAME("DISTRIBUTOR_NAME", "Distributor_Name"),
    CREDIT("CREDIT", "Credit"),
    CASH("CASH", "Cash"),
    ADVANCE("ADVANCE", "Advance"),
    REFERENCE_NO("ADVANCE", "Reference_No");

    private String code;
    private String name;

    DistributorOpeningBalanceTemplate(String code, String name) {
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
