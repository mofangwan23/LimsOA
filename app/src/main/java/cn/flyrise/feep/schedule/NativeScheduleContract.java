package cn.flyrise.feep.schedule;

import java.util.List;

import cn.flyrise.android.protocol.entity.schedule.AgendaResponseItem;

/**
 * @author ZYP
 * @since 2016-11-29 13:51
 */
public interface NativeScheduleContract {

    interface IView {
        void showLoading();

        void hideLoading();

        void displayCurrentDate(String date, String week);

        void displayScheduleList(List<AgendaResponseItem> scheduleItems);

        void displayAgendaPromptInMonthView(List<Integer> promptLists);
    }

    interface IPresenter {

        void start();

        void requestSchedule(String date);

        void removeSchedule(String scheduleId);

        String getSeletedDate();

        void forceRefresh();
    }

}
