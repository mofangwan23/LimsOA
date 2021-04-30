package cn.flyrise.feep.location.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.K;
import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.Sign.loadMore;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import cn.flyrise.feep.location.bean.LocationSearchDataFilter;
import cn.flyrise.feep.location.bean.LocationSignTime;
import cn.flyrise.feep.location.bean.SignPoiItem;
import cn.flyrise.feep.location.contract.LocationQueryPoiContract;
import cn.flyrise.feep.location.contract.LocationWorkingContract;
import cn.flyrise.feep.location.contract.WorkingTimerContract;
import cn.flyrise.feep.location.contract.WorkingTimerContract.WorkingTimerListener;
import cn.flyrise.feep.location.model.LocationQueryPoiItemModel;
import cn.flyrise.feep.location.model.LocationWorkingModel;
import cn.flyrise.feep.location.util.RxWorkingTimer;
import cn.flyrise.feep.location.views.SignInMainSearchActivity;
import cn.flyrise.feep.location.views.SignInSearchActivity;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-3-22-15:54.
 */

public class LocationSearchSignPresenter implements FEListContract.Presenter
		, LocationWorkingContract.WorkingListener
		, WorkingTimerListener
		, LocationQueryPoiItemModel.OnQueryPoiItemListener {

	public final static int TYPE = K.location.LOCATION_SEARCH;
	private final static int REQUESTE_LOCATION = 1013;//重新请求考勤组
	private final static int SIGN_SUCCESS = 1016;//重新请求考勤组
	public final static int REQUEST_SIGN_IN = 1025;//签到请求

	private SignInSearchActivity mView;
	private Context mContext;

	private LocationWorkingContract mWorkingModel;
	private LocationQueryPoiContract mQueryPoiItem;
	private WorkingTimerContract mWorkingTimer;
	private String searchKey;

	private Handler mHandle = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == REQUESTE_LOCATION && mWorkingModel != null && mView != null) {
				mView.setLocationAdapterEnabled(true);
				mView.setSwipeRefreshEnabled(true);
				mView.unlockAdapter();
				mWorkingModel.requestWQT(TYPE);
			}
		}
	};

	public void initPresenter(Context context) {
		mView = (SignInSearchActivity) context;
		mContext = context;
		mWorkingModel = new LocationWorkingModel(context, this);
		mQueryPoiItem = new LocationQueryPoiItemModel(context, this);
		mWorkingTimer = new RxWorkingTimer(this);
		mWorkingModel.requestWQT(TYPE);
	}

	@Override
	public void onStart() {

	}

	@Override
	public void refreshListData() {

	}

	public void restartRefreshListData() {
		mQueryPoiItem.SearchKey(TYPE, searchKey);
	}

	@Override
	public void refreshListData(String searchKey) {
		this.searchKey = searchKey;
		mQueryPoiItem.SearchKey(TYPE, searchKey);
	}

	@Override
	public void loadMoreData() {
		mQueryPoiItem.loadMorePoiSearch();
	}

	@Override
	public boolean hasMoreData() {
		return mQueryPoiItem.hasMoreData();
	}

	@Override
	public void gpsLocationSuccess(LatLng currentLatlng) {
		showSwipeRefresh(true);
		mWorkingModel.setCanReport(true);
		mWorkingModel.setResponseSignStyle();
		mView.mAdapter.setNotAllowSuperRange(mWorkingModel.getStyle() == K.sign.STYLE_ATT);
		mQueryPoiItem.requestLoactionPoiItem(mWorkingModel.getStyle(), mWorkingModel.signRange());
	}

	@Override
	public void showSwipeRefresh(boolean isShow) {
		mView.showRefreshLoading(isShow);
	}

	@Override
	public void setEmptyingAdapter() {
		mView.setCanPullUp(false);
	}

	@Override
	public void refreshListData(List<PoiItem> items) {
		mView.setSearchKey(searchKey);
		mView.showLocationSaveListView(CommonUtil.isEmptyList(items) && !mView.isSaveEmpty());
		mView.refreshListData(getLocationSearchDataFilter(items));
		mView.setCanPullUp(!CommonUtil.isEmptyList(items) && mQueryPoiItem.hasMoreData());
	}

	@Override
	public void loadMoreListData(List<PoiItem> items) {
		mView.showLocationSaveListView(false);
		mView.loadMoreListData(getLocationSearchDataFilter(items));
		mView.setCanPullUp(hasMoreData());
	}

	private List<SignPoiItem> getLocationSearchDataFilter(List<PoiItem> items) {
		return new LocationSearchDataFilter.Builder()
				.setType(TYPE)
				.setNotAllowSuperRange(mWorkingModel.getStyle() == K.sign.STYLE_ATT)
				.setItems(items)
//				.setSearch(LocationQueryPoiContract.search)
				.setSearch(mWorkingModel.signRange())
				.setSignLatLng(mWorkingModel.signLatLng())
				.setSignRange(mWorkingModel.signRange())
				.builder()
				.getData();
	}

	@Override
	public void loadMoreListFail() {
		mView.refreshListData(null);
		mView.showLocationSaveListView(TextUtils.isEmpty(searchKey));
		mView.loadMoreListFail();
	}

	@Override
	public void loadMoreState(int state) {
		if (state == loadMore.can_load_more) {
			mView.mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING);
		}
		else if (state == loadMore.no_load_more) {
			mView.mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_END);
		}
		else if (state == loadMore.success_load_more) {
			mView.mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE);
		}
	}

	@Override
	public void workingTimerExistence(String serviceTime, boolean isSignMany) {
		mWorkingTimer.startServiceDateTimer(serviceTime);
		mWorkingModel.distanceSignTime(mWorkingTimer.getTimeInMillis());
	}

	@Override
	public void workingTimeRestartRequestWQT() {

	}

	@Override
	public void workingRequestEnd() {
		if (LoadingHint.isLoading()) LoadingHint.hide();
		if (!TextUtils.isEmpty(searchKey)) mQueryPoiItem.SearchKey(TYPE, searchKey);
	}

	@Override public void workingLeaderListener(boolean isLeader) {

	}

	@Override
	public void notifyRefreshServiceTime(LocationSignTime signData) {//定时器更新服务器时间
		mWorkingModel.distanceSignTime(mWorkingTimer.getTimeInMillis());
	}

	public void signSelectedPoiItem(LocationSaveItem saveItem) {//选中某一项
		SignInMainSearchActivity.Companion.start((AppCompatActivity) mContext, saveItem, REQUEST_SIGN_IN);
	}

	public void onPause() {
		mQueryPoiItem.destroyLocationGps();
	}

	public void onDestroy() {
		mHandle.removeMessages(REQUESTE_LOCATION);
		mHandle.removeMessages(SIGN_SUCCESS);
		mWorkingModel = null;
		mQueryPoiItem = null;
		if (mWorkingTimer != null) {
			mWorkingTimer.onDestroy();
			mWorkingTimer = null;
		}
	}
}
