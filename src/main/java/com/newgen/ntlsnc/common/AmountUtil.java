package com.newgen.ntlsnc.common;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author liton
 * Created on 9/14/22 3:14 PM
 */
public class AmountUtil {
    public static Float round(Float value, int decimalPlace){
        if(decimalPlace <= 0){
            return value;
        }

        StringBuilder pattern = new StringBuilder("#.");
        for(int i=0; i<decimalPlace; i++){
            pattern.append("#");
        }

        DecimalFormat df = new DecimalFormat(pattern.toString());
        df.setRoundingMode(RoundingMode.HALF_UP);

        return Float.parseFloat(df.format(value));
    }

    public static Double round(Double value, int decimalPlace){
        if(decimalPlace <= 0){
            return value;
        }

        StringBuilder pattern = new StringBuilder("#.");
        for(int i=0; i<decimalPlace; i++){
            pattern.append("#");
        }

        DecimalFormat df = new DecimalFormat(pattern.toString());
        df.setRoundingMode(RoundingMode.HALF_UP);

        return Double.parseDouble(df.format(value));
    }
}
