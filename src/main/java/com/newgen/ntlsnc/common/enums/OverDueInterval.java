package com.newgen.ntlsnc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Newaz Sharif
 * @since 11th Aug, 22
 */

public enum OverDueInterval {
    THIRTY("THIRTY", "Thirty"),
    SIXTY("SIXTY", "Sixty"),
    NINETY("NINETY", "Ninety"),
    ONETWENTY("ONETWENTY", "One Twenty"),
    ONEEIGHTY("ONEEIGHTY", "One Eighty"),
    ONEEIGHTYPLUS("ONEEIGHTYPLUS", "One Eighty Plus"),
    DEFAULT("DEFAULT", "Default");

    private String code;
    private String name;

    OverDueInterval(String code, String name) {
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
