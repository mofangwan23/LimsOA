package cn.flyrise.feep.location.contract;

import cn.flyrise.feep.location.bean.SignInLeaderMonthDetail;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-11:20.
 * 月统计详情
 */

public interface SignInLeaderMonthDetailContract {

	interface IPresenter {

		void requestMonthDetail(String month, int type);//month:2018-03
	}

	interface IView {

		String MONTH = "current_month";

		String TYPE = "current_type";

		String title = "current_title";

		void resultData(List<SignInLeaderMonthDetail> summaryItems);

		void resultError();
	}
}
