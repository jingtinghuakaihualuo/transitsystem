package com.example.transitsystem.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeStampUtil {

    public static Long getCurentTimeStamp10() {
        long time = System.currentTimeMillis();
        String leng_10_str = String.valueOf(time).substring(0, 10);
        return Long.valueOf(leng_10_str);
    }

    public static String fromDateToString(Long timeStamp) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date data = new Date(timeStamp);
        return sdf.format(data);
    }

    public static long fromDateToLong(String data) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(data);
        long timeStamp = date.getTime();

        return timeStamp / 1000;
    }

    public static String getStringDatetime(String time) {
        //此方法是将2017-11-18T07:12:06.615Z格式的时间转化为秒为单位的Long类型。
//        String time = "2017-11-30T10:41:44.651Z";
        time = time.replace("Z", " UTC");//UTC是本地时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        Date d = null;
        try {
            d = format.parse(time);
        } catch (ParseException e) {
        // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //此处是将date类型装换为字符串类型，比如：Sat Nov 18 15:12:06 CST 2017转换为2017-11-18 15:12:06
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return sf.format(d);
    }

    public static Long getLongDatetime(String time) {
        //yyyy-MM-dd'T'HH:mm:ss.sss
        //2019-10-24T22:12:00.000+08:00
        Long timeStamp = null;
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date date = dateFormat.parse(time);
            timeStamp =date.getTime();
        }catch(Exception e){
            timeStamp = null;
        }
        return timeStamp == null ? null : timeStamp/1000;
    }

}
