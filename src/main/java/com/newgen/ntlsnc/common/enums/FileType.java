package com.newgen.ntlsnc.common.enums;

/**
 * @author Newaz Sharif
 * @since 22th June, 22
 */
public enum FileType {

    PHOTO("PHOTO", "Photo"),
    NID("NID", "nid"),
    DOCUMENT("DOCUMENT", "Document"),
    LOGO("LOGO","logo");

    private String code;
    private String name;


    FileType(String code, String name) {
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
