package cn.flyrise.feep.location.contract;

import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.location.bean.LocusPersonLists;
import java.util.Date;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-5-14:28.
 */

public interface SignInCalendarContract {

	interface IPresenter {

		int perPageNums = Integer.MAX_VALUE;

		Date textToDate(String yearMonth);

		void requestSignHistory(String userId, String month);//2018-05,month为空，默认选今天

		void requestSignHistoryDay(String date);//2018-05-05查看某一天考勤数据

		void requestSignHistoryMonth(String date);//2018-05

		String getCurrentUserId();
	}

	interface IView {

		void displayList(List<FEListItem> items);

		void displayAgendaPromptInMonthView(List<Integer> promptLists);

		void setLocationSignSummary(String count, String interval);

		void setCurrentYears(String years);

	}
}
