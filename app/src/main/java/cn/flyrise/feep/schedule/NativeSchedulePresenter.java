package cn.flyrise.feep.schedule;

import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.List;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.schedule.data.NativeScheduleDataSource;
import cn.flyrise.feep.schedule.data.ScheduleDataRepository;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2016-11-29 13:51
 */
public class NativeSchedulePresenter implements NativeScheduleContract.IPresenter {

    private static final String[] WEEKS = CoreZygote.getContext().getResources().getStringArray(R.array.schedule_weeks);
    private NativeScheduleContract.IView mScheduleView;
    private NativeScheduleDataSource mDataSources;

    private String mCurrentDate;
    private boolean forceRefresh = true;

    public NativeSchedulePresenter(NativeScheduleContract.IView scheduleView) {
        this.mScheduleView = scheduleView;
        this.mDataSources = new NativeScheduleDataSource(new ScheduleDataRepository());
    }

    @Override public void start() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        requestSchedule(year + "." + month + "." + day);
    }

    @Override public void requestSchedule(String date) {
        String[] dates = date.split("\\.");
        int year = CommonUtil.parseInt(dates[0]);
        int month = CommonUtil.parseInt(dates[1]);
        int day = CommonUtil.parseInt(dates[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        String strWeek = WEEKS[week - 1];

        String yyyyMM = dates[0] + "-" + dates[1];
        mScheduleView.displayCurrentDate(dates[2], strWeek);
        mScheduleView.showLoading();
        mDataSources.getAgendaList(yyyyMM, day, forceRefresh)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(scheduleItems -> {
                    forceRefresh = false;
                    mCurrentDate = date;
                    mScheduleView.hideLoading();
                    mScheduleView.displayScheduleList(scheduleItems);
                    mScheduleView.displayAgendaPromptInMonthView(
                            mDataSources.getAgendaDays(forceRefresh || DateUtils.isToday(calendar.getTimeInMillis())));
                }, exception -> {
                    forceRefresh = false;
                    exception.printStackTrace();
                    mScheduleView.hideLoading();
                    mScheduleView.displayScheduleList(null);
                });
    }

    @Override public void removeSchedule(String scheduleId) {
        boolean result = mDataSources.removeScheduleItem(scheduleId);
        FELog.i("schedule remove result : " + result);

        List<Integer> scheduleItems = mDataSources.getAgendaDays(true);
        mScheduleView.displayAgendaPromptInMonthView(scheduleItems);
    }

    @Override public String getSeletedDate() {
        return mCurrentDate;
    }

    @Override public void forceRefresh() {
        this.forceRefresh = true;
    }


}
