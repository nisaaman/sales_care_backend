package com.newgen.ntlsnc.common.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sunipa
 * @date ১৯/৪/২২
 * @time ১০:২৬ AM
 */
public enum PaymentType {

    TT("TT", "TT"),
    CASH("CASH", "Cash"),
    DD("DD", "DD"),
    CHEQUE("CHEQUE", "Cheque"),
    ONLINE("ONLINE", "Online");

    private String code;
    private String name;

    PaymentType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "PaymentType{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public static List<Map<String, String>> getAll(){
        List<Map<String, String>> mapList = new ArrayList<>();

        for(PaymentType paymentType : PaymentType.values()){
            Map<String, String> map = new HashMap<>();
            map.put("code", paymentType.code);
            map.put("name", paymentType.name);

            mapList.add(map);
        }

        return mapList;
    }
}
