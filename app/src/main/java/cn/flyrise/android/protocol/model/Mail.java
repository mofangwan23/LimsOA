package cn.flyrise.android.protocol.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author ZYP
 * @since 2016/7/13 10:11
 */
public class Mail {

    public static final Calendar sCalendar = Calendar.getInstance();
    public static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");

    /**
     * 邮件ID
     */
    @SerializedName("mail_id") public String mailId;

    /**
     * 邮件状态
     * "0":已收带有附件
     * "1":已收没有附件
     * "2":未收带有附件
     * "3":未收没有附件
     */
    public String status;

    /**
     * 邮件发送人
     */
    @SerializedName("SendMan") public String sendMan;

    /**
     * 邮件标题
     */
    public String title;

    /**
     * 邮件发送日期
     * 2016-07-13 10:24
     */
    @SerializedName("SendTime") public String sendTime;

    /**
     * 邮件摘要，前 50 字
     */
    public String summary;

    /**
     * 接收人列表
     */
    public String tto;

    /**
     * 发送人 UserId
     */
    @SerializedName("SendUserId") public String sendUserId;

    public String getTime() {
        try {
            Date date = sDateFormat.parse(sendTime);
            sCalendar.setTime(date);
            int hour = sCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = sCalendar.get(Calendar.MINUTE);
            String m = (minute <= 9) ? "0" + minute : "" + minute;
            String h = (hour <= 9) ? "0" + hour : "" + hour;
            return h + ":" + m;
        } catch (ParseException e) {
            return "";
        }
    }

    public String getDate() {
        try {
            Date date = sDateFormat.parse(sendTime);
            sCalendar.setTime(date);
            int month = sCalendar.get(Calendar.MONTH) + 1;
            int day = sCalendar.get(Calendar.DAY_OF_MONTH);
            return month + "月" + day + "日";
        } catch (ParseException e) {
            return "";
        }
    }

    public boolean isSameMonth(Mail mail) {
        return TextUtils.equals(this.getDate(), mail.getDate());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mail mail = (Mail) o;

        if (!mailId.equals(mail.mailId)) return false;
        if (!sendMan.equals(mail.sendMan)) return false;
        if (!title.equals(mail.title)) return false;
        if (!sendTime.equals(mail.sendTime)) return false;
        return sendUserId.equals(mail.sendUserId);

    }

    @Override public int hashCode() {
        int result = mailId.hashCode();
        result = 31 * result + sendMan.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + sendTime.hashCode();
        result = 31 * result + sendUserId.hashCode();
        return result;
    }
}
