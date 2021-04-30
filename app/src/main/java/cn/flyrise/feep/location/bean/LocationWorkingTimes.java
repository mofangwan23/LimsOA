package cn.flyrise.feep.location.bean;

import android.support.annotation.Keep;

/**
 * 类描述：考勤时间
 *
 * @author 罗展健
 * @version 1.0
 * @date 2015年3月25日 上午11:27:07
 */
@Keep
public class LocationWorkingTimes {

    private String times;          // 打卡时间段
    private String type;           // 打卡类型
    private String serviceTime;    // 服务器时间
    private String forced = "1";   // 是否强制拍照
    private String eachTime;       // 每隔多久上报一次
    private String hasSubordinate; // 是否有下属
    private String beTime;         // 上下班时间

    private String signMany;      //是否签到多次,"1"为多次签到

    public String getSignMany() {
        return signMany;
    }

    public void setSignMany(String signMany) {
        this.signMany = signMany;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(String serviceTime) {
        this.serviceTime = serviceTime;
    }

    public String getEachTime() {
        return eachTime;
    }

    public void setEachTime(String eachTime) {
        this.eachTime = eachTime;
    }

    public String getForced() {
        return forced;
    }

    public void setForced(String forced) {
        this.forced = forced;
    }

    public String getHasSubordinate() {
        return hasSubordinate;
    }

    public void setHasSubordinate(String hasSubordinate) {
        this.hasSubordinate = hasSubordinate;
    }

    public String getBeTime() {
        return beTime;
    }

    public void setBeTime(String beTime) {
        this.beTime = beTime;
    }

}
