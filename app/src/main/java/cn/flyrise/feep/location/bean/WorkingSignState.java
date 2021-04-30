package cn.flyrise.feep.location.bean;

import com.amap.api.maps.model.LatLng;

/**
 * 新建：陈冕;
 * 日期： 2017-12-18-19:04.
 * 考勤签到的状态
 */

public class WorkingSignState {

    public boolean isCanReport = false; //到了打卡时间

    public boolean hasSubordinate = false; //判断是否有下属

    public boolean isLeader = false; //判断是否是上司

    public boolean hasTimes = false; //判断是否在考勤组

    public boolean isSignMany = false; //多次签到

    public LatLng signLatLng; //考勤组经纬度

    public int signRange; //考勤范围

    public String endWorkingSignTime;//考勤结束时间

}
