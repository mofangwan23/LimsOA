package cn.flyrise.feep.location.contract;

import cn.flyrise.feep.location.bean.LocationSignTime;
import cn.flyrise.feep.location.event.EventLocationSignSuccess;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-8-9-9:16.
 * 实现签到必须继承的类
 */
public class SignInMustContract implements LocationQueryPoiContract.OnQueryPoiItemListener
		, LocationWorkingContract.WorkingListener
		, LocationReportSignContract.ReportSignListener
		, WorkingTimerContract.WorkingTimerListener {

	@Override
	public void gpsLocationSuccess(LatLng currentLatlng) {

	}

	@Override
	public void showSwipeRefresh(boolean isShow) {

	}

	@Override
	public void setEmptyingAdapter() {

	}

	@Override
	public void refreshListData(List<PoiItem> items) {

	}

	@Override
	public void loadMoreListData(List<PoiItem> items) {

	}

	@Override
	public void loadMoreListFail() {

	}

	@Override
	public void loadMoreState(int state) {

	}

	@Override
	public void onReportSetCheckedItem() {

	}

	@Override
	public void onReportFailure(String errorText,int error) {

	}

	@Override
	public void onReportHistorySuccess(EventLocationSignSuccess signSuccess) {

	}

	@Override public void onReportPhotoDismiss(boolean isSurePhoto) {

	}

	@Override
	public void workingTimerExistence(String serviceTime, boolean isSignMany) {

	}

	@Override
	public void workingTimeRestartRequestWQT() {

	}

	@Override
	public void workingRequestEnd() {

	}

	@Override
	public void workingLeaderListener(boolean isLeader) {

	}

	@Override
	public void notifyRefreshServiceTime(LocationSignTime signData) {

	}
}
