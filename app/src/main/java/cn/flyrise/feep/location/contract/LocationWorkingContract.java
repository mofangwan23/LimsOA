package cn.flyrise.feep.location.contract;

import cn.flyrise.feep.location.bean.WorkingSignState;
import com.amap.api.maps.model.LatLng;

/**
 * 新建：陈冕;
 * 日期： 2017-12-19-9:54.
 * 考勤组请求
 */

public interface LocationWorkingContract {

    void requestWQT(int locationType);//加载是否有外勤通的数据

    String distanceSignTime(long timeInMillis);//距离签到时间

    int getStyle();//签到界面的样式

    void setResponseSignStyle();//判断考勤组签到模式

    boolean isCanReport();//当前时间是否可以打卡

    void setCanReport(boolean isSign);//是否可以打卡

    int signRange();//考勤范围

    LatLng signLatLng();//考勤坐标

    boolean hasTimes();//判断是否在考勤组

    boolean isSignMany();//多次签到

    boolean isLeaderOrSubordinate();//是领导或者存在下属

    boolean isPhotoSign();//是否为强制拍照签到

    boolean isWorkingTimeNull();//考勤组时间为空

    String getTimes(); // 打卡时间段

    String getForced();// 是否强制拍照

    String getType(); // 打卡类型

    String getServiceTime();// 服务器时间

    String getPname(); //地点名称

    String getPaddress(); //地址详情

    String getLatitude(); //维度

    String getLongitude(); //经度

    String getRange(); //考勤范围

    String getEndWorkingSignTime();//结束考勤时间

    WorkingSignState getWorkingSignState();

    interface WorkingListener {

        void workingTimerExistence(String serviceTime, boolean isSignMany);//考勤组时间存在,更新考勤布局

        void workingTimeRestartRequestWQT();//时间到达临界值，刷新签到记录

        void workingRequestEnd();//考勤组请求结束，开始定位

        void workingLeaderListener(boolean isLeader);//是否为领导
    }

}
