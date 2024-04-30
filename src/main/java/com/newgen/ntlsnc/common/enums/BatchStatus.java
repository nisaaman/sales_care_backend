package com.newgen.ntlsnc.common.enums;

/**
 * @author Newaz Sharif
 * @since 23th Aug, 22
 */
public enum BatchStatus {

    COMPLETED("COMPLETED", "Completed"),
    STARTING("STARTING", "Starting"),
    STARTED("STARTED", "Started"),
    STOPPING("STOPPING", "Stopping"),
    STOPPED("STOPPED", "Stopped"),
    FAILED("FAILED", "Failed"),
    ABANDONED("ABANDONED", "Abandoned"),
    UNKNOWN("UNKNOWN", "Unknown");

    private String code;
    private String name;

    BatchStatus(String code, String name) {
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
