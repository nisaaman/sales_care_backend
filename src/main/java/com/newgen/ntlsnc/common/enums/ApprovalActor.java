package com.newgen.ntlsnc.common.enums;

/**
 * @author nisa
 * @date 9/10/22
 * @time 4:56 PM
 */
public enum ApprovalActor {
    ROLE("ROLE", "Role"),
    DESIGNATION("DESIGNATION", "Designation"),
    LOCATION_TYPE("LOCATION_TYPE", "Location Type"),
    DEPOT_IN_CHARGE("DEPOT_IN_CHARGE", "Depot In Charge"),
    APPLICATION_USER("APPLICATION_USER", "Application User");

    private String code;
    private String name;
    ApprovalActor(String code, String name) {
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
