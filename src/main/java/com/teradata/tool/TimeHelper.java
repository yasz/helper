package com.teradata.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Peter.Yang on 2017/12/18.
 */
public class TimeHelper {
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    public static long str2unix(String txdate) throws ParseException {
        return dateFormat.parse(txdate).getTime();
    }
    public static String getCurrentTime(){
        return timeFormat.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentDate(){
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentDatetime(){
        return getCurrentDate()+" "+getCurrentTime();
    }
    public static String calcTxMonth (String txdate,int offset) {
        Calendar cl = Calendar.getInstance();
        try {
            cl.setTime(dateFormat.parse(txdate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cl.add(2,offset);
        return dateFormat.format(cl.getTime()).substring(0,6);
    }
    public static String calcTxDate (String txdate,int offset) {
        Calendar cl = Calendar.getInstance();
        try {
            cl.setTime(dateFormat.parse(txdate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cl.add(6,offset);
        return dateFormat.format(cl.getTime());
    }

    public static void main(String[] args) {
        String txdate= "20171218";
        System.out.println(getCurrentDatetime());
        System.out.println(calcTxDate(txdate,-1));
        System.out.println(calcTxMonth(txdate,-10));
    }

}
