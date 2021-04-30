package cn.flyrise.feep.location.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.flyrise.android.shared.utility.FEDate;
import cn.flyrise.feep.location.bean.LocationSignTime;
import cn.flyrise.feep.location.contract.WorkingTimerContract;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 新建：陈冕;
 * 日期： 2017-12-20-14:27.
 * 定时器
 */

public class RxWorkingTimer implements WorkingTimerContract {

    private Calendar calendar;

    private Subscription subscribe;

    private WorkingTimerListener mListener;

    public RxWorkingTimer(WorkingTimerListener listener) {
        this.mListener = listener;
    }

    //开启服务端时间定时器
    @Override
    public void startServiceDateTimer(String dateStr) {
        calendar = Calendar.getInstance();
        calendar.setTime(new Date(FEDate.getDateSS(dateStr).getTime()));
        startTimer();
    }

    private void startTimer() {//开启定时器
        if (subscribe != null) {
            return;
        }
        subscribe = Observable.interval(REFRESH_TIME, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (mListener == null) {
                            return;
                        }
                        goRefreshDateTime();
                        mListener.notifyRefreshServiceTime(getLocationSignData());
                    }
                });
    }

    private void goRefreshDateTime() { //每秒刷新时间
        if (calendar == null) {
            return;
        }
        calendar.setTime(new Date(calendar.getTimeInMillis() + 1000));
    }

    private LocationSignTime getLocationSignData() {
        return LocationSignDate.subSignData(calendar);
    }

    @Override
    public long getTimeInMillis() {
        return calendar.getTimeInMillis();
    }

    @Override
    public String getCurrentServiceTime(String serviceTime) {
        return calendar == null ? serviceTime : calendar2StringDateTime(calendar);
    }

    private String calendar2StringDateTime(Calendar calendar) {
        return calendar == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"
                , Locale.getDefault()).format(calendar.getTime());
    }

    @Override
    public void onDestroy() {
        if (subscribe != null) {
            subscribe.unsubscribe();
        }
    }
}
