package com.newgen.ntlsnc.common;

import com.newgen.ntlsnc.common.enums.Month;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sunipa
 * @date ৩/১/২৩
 */
public class DateUtil {

    public static List<Integer> monthListBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<Integer> monthList = new ArrayList<>();

        while (startDate.isBefore(endDate)) {
            int month = startDate.getMonthValue();//System.out.println(startDate.format(formatter));
            monthList.add(month);
            startDate = startDate.plusMonths(1);
        }
        return monthList;
    }
}
