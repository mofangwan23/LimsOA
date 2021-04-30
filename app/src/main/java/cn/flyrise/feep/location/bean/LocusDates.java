package cn.flyrise.feep.location.bean;

import android.support.annotation.Keep;

import java.io.Serializable;

/**
 * 类描述：考勤轨迹的日期类
 * 
 * @author 罗展健
 * @date 2015年3月24日 上午10:30:07
 * @version 1.0
 */
@Keep
public class LocusDates implements Serializable {

    private static final long serialVersionUID = 41L;
    private String            name;
    private String            date;
    private String            week;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

}
