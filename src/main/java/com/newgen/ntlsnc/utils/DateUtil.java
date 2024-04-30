package com.newgen.ntlsnc.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author sagor
 * @date ১৭/৫/২২
 */
public class DateUtil {


    private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
        put("^\\d{8}$", "yyyyMMdd");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "dd/MM/yyyy");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
        //put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
        put("^\\[A-Z][a-z]{2}\\s\\d{1,2},\\s\\d{4}$", "MMM, dd yyyy"); //[A-Z][a-z]{2}\s\d{2},\s\d{4}  Jun 30, 2018
        put("^\\d{1,2}\\s[a-zA-Z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
        put("^\\d{12}$", "yyyyMMddHHmm");
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
        put("^\\d{14}$", "yyyyMMddHHmmss");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd/MM/yyyy HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$", "yyyy-MM-dd HH:mm:ss.SSS");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s\\w\\w$", "dd/MM/yyyy hh:mm:ss a");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$/", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }};

    public static Calendar getCalendar (Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);

        return calendar;
    }

    public static SimpleDateFormat determineDateFormat(String dateString) {

        for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {

            if (dateString.toLowerCase().matches(regexp)) {

                return new SimpleDateFormat(DATE_FORMAT_REGEXPS.get(regexp));
            }
        }

        return new SimpleDateFormat();
    }


    public static java.sql.Timestamp objectToTimestamp ( Object value, DateFormat IN_TIMESTAMP_FORMAT ){

        try {

            if( value == null ) return null;
            if( value instanceof java.sql.Timestamp ) return (java.sql.Timestamp)value;

            if( value instanceof String ) {

                if( "".equals( (String)value ) ) return null;
                return new java.sql.Timestamp( IN_TIMESTAMP_FORMAT.parse( (String)value ).getTime() );
            }

            return new java.sql.Timestamp( IN_TIMESTAMP_FORMAT.parse( value.toString() ).getTime() );

        } catch(Exception ex) {

            return null;
        }
    }

    public static Date getCurrentDateWithStartTime(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 01);
        calendar.set(Calendar.MILLISECOND, 000);

        return calendar.getTime();
    }

    public static Date getCurrentDateWithEndTime(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 000);

        return calendar.getTime();
    }

    public static Date getUtilDateFromString(String date) {

        try {

            SimpleDateFormat format = determineDateFormat(date);
            return  format.parse(date);
        } catch (ParseException e) {

        }

        return null;
    }

    public static LocalDate getLocalDateFromString(String date, String desireDateFormatter) {
        try {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(desireDateFormatter);
            return LocalDate.parse(date, formatter);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static java.sql.Date getTimestampToSQLDate(Timestamp timestamp) {

        try {
            return new java.sql.Date(timestamp.getTime());
        } catch (Exception e) { return null; }
    }

    public static java.sql.Timestamp getTimestampFromUtilDate(Date date) {

        try {
            return new java.sql.Timestamp(date.getTime());
        } catch (Exception e) { return null; }
    }

    public static java.sql.Timestamp getTimestampFromStringDate (String date) {

        try {
            SimpleDateFormat dateFormat = determineDateFormat(date);
            Date parsedDate = dateFormat.parse(date);
            return new java.sql.Timestamp(parsedDate.getTime());

        } catch (Exception e) { return null; }
    }

    public static String getStringDateFromDate(java.util.Date date, DateFormat dateFormat) {

        return dateFormat.format(date);
    }

    public static String getStringDateFromDate(LocalDate date, String dateFormatter) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(dateFormatter);
        return dateFormat.format(date);
    }

    public static String getStringDateFromSqlDate(java.sql.Date date, DateFormat dateFormat) {

        if(date != null) {

            return dateFormat.format(date);
        }

        return "";
    }

    public static boolean isLeapYear (int year) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);

        return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
    }

    public static Long getDaysDifferentBetweenTwoDate(LocalDate fromDate, LocalDate thruDate) {
        return ChronoUnit.DAYS.between(thruDate, fromDate);
    }

    public int getCurrentYear() {

        return Calendar.getInstance().get(Calendar.YEAR);

    }

    public static List<Integer> getMonthListByDateRange(LocalDate startDate, LocalDate endDate){
        long numOfDaysBetween = ChronoUnit.MONTHS.between(startDate, endDate) +1;
        List<Integer> monthList = new ArrayList<>();
        LocalDate finalStartDate = startDate;
        for (int i = 0; i < numOfDaysBetween; i++) {
            monthList.add(finalStartDate.plusMonths(i).getMonthValue());
        }
        return monthList;
    }

}
