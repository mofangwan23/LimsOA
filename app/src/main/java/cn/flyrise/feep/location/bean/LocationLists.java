package cn.flyrise.feep.location.bean;

import android.support.annotation.Keep;

import java.io.Serializable;

/**
 * 类描述：考勤轨迹的位置列表类
 * 
 * @author 罗展健
 * @date 2015年3月24日 上午10:31:43
 * @version 1.0
 */
@Keep
public class LocationLists implements Serializable {

    private static final long serialVersionUID = 42L;
    private String            latitude;
    private String            longitude;
    private String            address;
    private String            date;                   // 日期
    private String            time;                   // 时间
    private String            whatDay;                // 星期几

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWhatDay() {
        return whatDay;
    }

    public void setWhatDay(String whatDay) {
        this.whatDay = whatDay;
    }

}
