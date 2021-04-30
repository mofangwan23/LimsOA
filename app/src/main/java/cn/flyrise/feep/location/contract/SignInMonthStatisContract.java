package cn.flyrise.feep.location.contract;

import cn.flyrise.feep.location.bean.SignInMonthStatisItem;
import java.util.Date;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-11:20.
 */

public interface SignInMonthStatisContract {

	interface IPresenter {

		void requestMonthAndUserId(String month, String userId);//month:2018-03

		Date textToDate(String yearMonth);
	}

	interface IView {

		String MONTH = "current_month";

		String USER_ID = "user_id";

		void resultData(List<SignInMonthStatisItem> summaryItems);

		void resultError();
	}
}
