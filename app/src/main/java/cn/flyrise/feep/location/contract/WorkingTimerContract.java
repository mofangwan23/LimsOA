package cn.flyrise.feep.location.contract;

import cn.flyrise.feep.location.bean.LocationSignTime;

/**
 * 新建：陈冕;
 * 日期： 2017-12-20-14:38.
 * 定时器
 */

public interface WorkingTimerContract {

    int REFRESH_TIME = 1000;//刷新时间间隔*毫秒

    void startServiceDateTimer(String dateStr);//开启定时器

    long getTimeInMillis();

    String getCurrentServiceTime(String serviceTime);//获取当前服务端时间

    void onDestroy();

    interface WorkingTimerListener {

        void notifyRefreshServiceTime(LocationSignTime signData);

    }

}
