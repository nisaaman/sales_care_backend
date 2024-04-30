package com.newgen.ntlsnc.common.enums;

/**
 * @author liton
 * Created on 1/11/23 12:33 PM
 */

public enum UserType {
    ROLE_SUPER_ADMIN("ROLE_SUPER_ADMIN", "Role Super Admin"),
    ROLE_ADMIN("ROLE_ADMIN", "Role Admin"),
    ROLE_USER("ROLE_USER", "Role User"),
    ROLE_SALES_OFFICER("ROLE_SALES_OFFICER", "Role Sales Officer"),
    ROLE_OTHER("ROLE_OTHER", "Role Other");

    private String code;
    private String name;

    UserType(String code, String name) {
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
