package cn.flyrise.feep.location.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.commonality.bean.FEListInfo;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.location.contract.LocationHistoryRequstContract;
import cn.flyrise.feep.location.contract.SignInCalendarContract;
import cn.flyrise.feep.location.contract.SignInCalendarContract.IView;
import cn.flyrise.feep.location.model.LocationHistoryModel;
import cn.flyrise.feep.location.model.LocationMonthCalendarModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 新建：陈冕;
 * 日期： 2018-5-5-14:29.
 */

public class SignInCalendarPresenter implements SignInCalendarContract.IPresenter
		, LocationHistoryRequstContract.RequstListener {

	private LocationHistoryRequstContract mHistoryRequst;
	private SignInCalendarContract.IView mView;
	private LocationMonthCalendarModel mCalendarModel;//存储一个月的签到数据
	private Context mContext;
	private String mCurrentUserId;
	private String mCurrentMonth;
	private String mCurrentDay;


	public SignInCalendarPresenter(Context context, IView view) {
		this.mContext = context;
		this.mView = view;
		mCalendarModel = new LocationMonthCalendarModel();
		mHistoryRequst = new LocationHistoryModel(context, true, RequestType.LocationHistory, this);
	}

	public void setCurrentDay(String day) {
		mCurrentDay = day;
	}

	public void requestSignHistory(String userId, String month, int sumId) {//根据用户id请求历史记录
		String years = TextUtils.isEmpty(month) ? TextUtils.isEmpty(mCurrentMonth) ? getCurrentYearMonth() : mCurrentMonth : month;
		String id = TextUtils.isEmpty(userId) ? CoreZygote.getLoginUserServices().getUserId() : userId;
		LoadingHint.show(mContext);
		this.mCurrentUserId = id;
		this.mCurrentMonth = years;
		mHistoryRequst.cancleRquestData();
		mView.setCurrentYears(years);
		mCalendarModel.clear();
		mHistoryRequst.request(years, 1, id, perPageNums, sumId);
	}

	@Override
	public void requestSignHistory(String userId, String month) {//根据用户id请求历史记录
		requestSignHistory(userId, month, 0);
	}

	@Override
	public void requestSignHistoryDay(String day) {
		this.mCurrentDay = day;
		Observable.create((Subscriber<? super List<FEListItem>> subscriber) ->
				subscriber.onNext(mCalendarModel.getFEListItemDay(day)))
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(items -> mView.displayList(items));
	}

	@Override
	public void requestSignHistoryMonth(String yearsMonth) {
		LoadingHint.show(mContext);
		this.mCurrentMonth = yearsMonth;
		mView.setCurrentYears(yearsMonth);
		if (mCalendarModel != null) mCalendarModel.clear();
		refreshSignCalendarView();
		mHistoryRequst.cancleRquestData();
		mHistoryRequst.request(yearsMonth, 1, mCurrentUserId, perPageNums);
	}

	@Override
	public String getCurrentUserId() {
		return mCurrentUserId;
	}

	@SuppressLint("SimpleDateFormat")
	private String getCurrentYearMonth() {//2018-08
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
		return formatter.format(Calendar.getInstance().getTime());
	}

	@Override
	public void refreshNums(int nums) {

	}

	@Override
	public void refreshHistoryData(FEListInfo mCurrentItemInfo, int pageNum) {
		LoadingHint.hide();
		if (mCurrentItemInfo != null) mCalendarModel.sqlistListItemData(mCurrentItemInfo.getListItems());
		refreshSignCalendarView();
	}

	private void refreshSignCalendarView() {
		if (mCalendarModel == null || mView == null) return;
		mView.displayList(getModelFEListItem());
		mView.displayAgendaPromptInMonthView(mCalendarModel.getExitSignDates());
	}

	private List<FEListItem> getModelFEListItem() {
		if (TextUtils.isEmpty(mCurrentDay))
			return mCalendarModel.getFEListItem(1);
		else
			return mCalendarModel.getFEListItemDay(mCurrentDay);
	}

//	private void setDaySignSummary(List<FEListItem> items) {//打卡次数
//		if (CommonUtil.isEmptyList(items)) {
//			mView.setLocationSignSummary("0", "0");
//			return;
//		}
//		if (items.size() == 1) {
//			mView.setLocationSignSummary(items.size() + "", "0");
//			return;
//		}
//		mView.setLocationSignSummary(items.size() + "", getIntervalTime(items.get(0), items.get(items.size() - 1)));
//	}

//	private String getIntervalTime(FEListItem start, FEListItem end) {
//		if (start == null || TextUtils.isEmpty(start.getTime()) || end == null || TextUtils.isEmpty(end.getTime())) return "0";
//		long interval = Math.abs(timeTextToLong(start.getTime()) - timeTextToLong(end.getTime()));
//		if (interval == 0) return "0";
//		return getTime(new DecimalFormat("0.0").format(interval / (60 * 60 * 1000f)));
//	}

//	private String getTime(String time) {
//		if (TextUtils.isEmpty(time)) return "0";
//		if (!time.contains(".0")) return time;
//		return time.substring(0, time.indexOf(".0"));
//	}

//	@SuppressLint("SimpleDateFormat")
////	private long timeTextToLong(String time) {
////		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
////		try {
////			return simpleDateFormat.parse(time).getTime();
////		} catch (ParseException e) {
////			e.printStackTrace();
////		}
////		return 0;
////	}

	@SuppressLint("SimpleDateFormat")
	public Date textToDate(String yearMonth) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return sdf.parse(yearMonth);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Date();
	}
}
