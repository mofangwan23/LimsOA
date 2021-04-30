package cn.flyrise.android.shared.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 类描述：date类
 *
 * @author 罗展健
 * @version 1.0
 * @date 2015年4月20日 下午12:52:10
 */
public class FEDate {

    /**
     * 获得某date <br>
     * 格式yyyy-MM-dd HH:mm:ss
     */
    public static Date getDateSS (String dateStr) {
        Date date;
        final SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
        try {
            date = dateFormat.parse (dateStr);
        } catch (final ParseException e) {
            date = new Date ();
            e.printStackTrace ();
        }
        return date;
    }

    /**
     * 获得某date <br>
     * 格式yyyy-MM-dd HH:mm
     */
    public static Date getDateMM (String dateStr) {
        Date date;
        final SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm"); // 设置时间格式
        try {
            date = dateFormat.parse (dateStr);
        } catch (final ParseException e) {
            date = new Date ();
            e.printStackTrace ();
        }
        return date;
    }

    /**
     * 获得某date <br>
     * 格式yyyy-MM-dd HH
     */
    public static Date getDateHH (String dateStr) {
        Date date;
        final SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd HH"); // 设置时间格式
        try {
            date = dateFormat.parse (dateStr);
        } catch (final ParseException e) {
            date = new Date ();
            e.printStackTrace ();
        }
        return date;
    }

    /**
     * 获得某date <br>
     * 格式yyyy-MM-dd
     */
    public static Date getDate (String dateStr) {
        Date date;
        final SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd"); // 设置时间格式
        try {
            date = dateFormat.parse (dateStr);
        } catch (final ParseException e) {
            date = new Date ();
            e.printStackTrace ();
        }
        return date;
    }
}
