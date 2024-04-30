package com.newgen.ntlsnc.common.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sunipa
 * @date ১৯/৪/২২
 * @time ১০:৩০ AM
 */
public enum PaymentNature {

    ADVANCE("ADVANCE", "Advance"),
    REGULAR("REGULAR", "Regular");

    private String code;
    private String name;

    PaymentNature(String code, String name) {
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
        return "PaymentNature{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public static List<Map<String, String>> getAll(){
        List<Map<String, String>> mapList = new ArrayList<>();

        for(PaymentNature paymentNature : PaymentNature.values()){
            Map<String, String> map = new HashMap<>();
            map.put("code", paymentNature.code);
            map.put("name", paymentNature.name);

            mapList.add(map);
        }

        return mapList;
    }
}
