package cn.flyrise.feep.location.bean;

/**
 * 新建：陈冕;
 * 日期： 2017-8-2-9:34.
 * 当前时间日期
 */

public class LocationSignTime {

    public String data;     //日期

    public int hour;     //时

    public int minute;  //分钟

    public int second;  //秒

    public String getServiceCurrentTime() { //服务断当前时间
        return data + " " + String.format("%02d:%02d:%02d", hour, minute, second);
    }

}
