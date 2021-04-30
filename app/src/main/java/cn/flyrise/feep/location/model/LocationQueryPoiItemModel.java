package cn.flyrise.feep.location.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.location.Sign.loadMore;
import cn.flyrise.feep.location.contract.LocationQueryPoiContract;
import cn.flyrise.feep.location.util.GpsHelper;
import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import java.util.List;

/**
 * cm 2017-3-14.
 */

public class LocationQueryPoiItemModel implements LocationQueryPoiContract
		, PoiSearchQueryModel.OnSerqQueryListener, GpsHelper.LocationCallBack {

	private GpsHelper gpsHelper;

	private int pageCount;
	private int currentPageNum = 0;//当前是第几页
	private int mLocationType;
	private int customType = TYPE_CUSTOM_CITY;//自定义考勤关键字搜索地点使用，自动切换1000米范围和全国搜索

	private String searchKay = "";
	private boolean isShowData = true;

	private Context mContext;
	private LatLonPoint curLatLonPoint = null;//当前坐标
	private LocationQueryPoiContract.OnQueryPoiItemListener mListener; //返回给界面的参数
	private PoiSearchQueryModel mQueryModel; //请求周边结果回调

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == search_char_error && !isShowData) {
				FEToast.showMessage(mContext.getResources().getString(R.string.location_custiom_search_error));
				if ((Integer) msg.obj == 0 && mListener != null) mListener.refreshListData(null);
			}
		}
	};

	public LocationQueryPoiItemModel(Context context, LocationQueryPoiContract.OnQueryPoiItemListener listener) {
		mContext = context;
		mListener = listener;
	}

	@Override
	public void stopLocationGps() {
		if (gpsHelper != null) gpsHelper.stopContinuouslyLocation();
	}

	@Override
	public void destroyLocationGps() {
		mHandler.removeMessages(search_char_error);
		if (gpsHelper != null) {
			gpsHelper.stopContinuouslyLocation();
			gpsHelper.destroyContinuouslyLocation();
			gpsHelper = null;
		}
	}

	@Override
	public void getGPSLocation(int locationType) {
		SearchKey(locationType, "");
	}

	@Override
	public void getRapidlyLocation(int locationType) {
		searchKey(locationType, "", true);
	}

	@Override
	public void SearchKey(int locationType, String key) {
		searchKey(locationType, key, false);
	}

	private void searchKey(int locationType, String key, boolean isRapidlyLocation) {
		currentPageNum = 0;
		this.mLocationType = locationType;
		this.searchKay = key;
		startInitGpsLocation(mLocationType, isRapidlyLocation);
	}

	@Override
	public void loadMorePoiSearch() {//加载更多
		if (curLatLonPoint == null || isLastPage(currentPageNum)) {
			if (mListener != null) mListener.loadMoreState(loadMore.no_load_more);
			return;
		}
		currentPageNum++;
		if (mQueryModel.isPoiSerchQueryNull() || currentPageNum == 0) {
			requestLoactionPoiItem();
			return;
		}
		mQueryModel.setPageNum(currentPageNum);// 设置查下一页
		mQueryModel.startQuery();
		if (mListener != null) mListener.loadMoreState(loadMore.can_load_more);
	}

	private boolean isLastPage(int curPageNum) { //是最后一页
		if ((pageCount - 1) > curPageNum) return false;
		if (curPageNum != 0) FEToast.showMessage(mContext.getResources().getString(R.string.lbl_text_no_more));
		return true;
	}

	private void startInitGpsLocation(int locationType, boolean isRapidlyLocation) {
		if (gpsHelper == null)
			gpsHelper = new GpsHelper(mContext, locationType, spaceTime, isRapidlyLocation, this);
		else
			gpsHelper.restartGpsLocation(locationType, isRapidlyLocation);
	}

	@Override
	public void success(AMapLocation location) {//定位成功
		if (!NetworkUtil.isNetworkAvailable(mContext))
			FEToast.showMessage(CommonUtil.getString(R.string.core_http_failure));
		try {
			locationLatLonOperation(location);
		} catch (Exception e) {
			hintLoadingView();
			e.printStackTrace();
		}
	}

	@Override
	public void error() {//定位失败
		if (!NetworkUtil.isNetworkAvailable(mContext))
			FEToast.showMessage(CommonUtil.getString(R.string.core_http_failure));
		hintLoadingView();
	}

	private void hintLoadingView() {
		if (LoadingHint.isLoading()) LoadingHint.hide();
	}

	private void locationLatLonOperation(AMapLocation location) {
		if (curLatLonPoint != null) curLatLonPoint = null;
		curLatLonPoint = new LatLonPoint(location.getLatitude(), location.getLongitude());
//		curLatLonPoint = CsLocationData.csLatLng();//模拟定位，足不出户，骚起来
		if (mListener != null) mListener.gpsLocationSuccess(new LatLng(curLatLonPoint.getLatitude(), curLatLonPoint.getLongitude()));
	}

	@Override
	public void requestLoactionPoiItem() {
		requestLoactionPoiItems(K.sign.STYLE_LIST, 0, searchKay);
	}

	@Override
	public boolean hasMoreData() {
		return (pageCount - 1) > currentPageNum;
	}

	@Override
	public void requestLoactionPoiItem(int signStyle, int workingRange) {
		requestLoactionPoiItems(signStyle, workingRange, searchKay);
	}

	@Override
	public void requestLoactionPoiItem(LatLonPoint latLonPoint) {//发送位置
		curLatLonPoint = latLonPoint;
		requestLoactionPoiItems(K.sign.STYLE_LIST, 0, "");
	}

	//根据地位得到的数据，查找周边公司名等信息
	private void requestLoactionPoiItems(int signStyle, int workingRange, String searchKey) {
		currentPageNum = 0;
		if (mListener != null) mListener.setEmptyingAdapter();
		try {
			mQueryModel = createPoiSearchBound(signStyle, workingRange, searchKey);
			mQueryModel.startQuery();
		} catch (Exception e) {
			hideSwipeRefresh();
			hintLoadingView();
			e.printStackTrace();
		}
	}

	private PoiSearchQueryModel createPoiSearchBound(int signStyle, int workingRange, String searchKay) {
		return new PoiSearchQueryModel.Builder(mContext, this)
				.setLocationType(mLocationType)
				.setSearchKay(searchKay)
				.setQueryRangeKey(poiSearchKey)
				.setPageSize(pageSize)
				.setCurrentPageNum(currentPageNum)
				.setCurrentLatLonPoint(curLatLonPoint)
				.setSearch(search)
				.setSignStyle(signStyle)
				.setWorkingRange(workingRange)
				.setCustomSearchAddressType(customType)
				.setCustomSearch(custom_search)
				.build();
	}

	@Override
	public void onQuerySuccess(int pageCount, List<PoiItem> items, int curPageNum) {//搜索周边成功
		if (mListener != null)
			mListener.loadMoreState((curLatLonPoint == null || isLastPage(currentPageNum)) && currentPageNum != 0
					? loadMore.no_load_more : loadMore.success_load_more);
		hintLoadingView();
		if (mListener == null) {
			hideSwipeRefresh();
			return;
		}
		this.pageCount = pageCount;
		if (isExceedDistanceQueryPoiItem(items, curPageNum)) {
			hideSwipeRefresh();
			return;
		}
		customType = TYPE_CUSTOM_CITY;
		isShowData = true;
		loadPoiSearchResult(items, curPageNum);
		hideSwipeRefresh();
	}

	@Override
	public void onQueryFailure() {
		if (mListener != null) mListener.loadMoreState(loadMore.success_load_more);
		hideSwipeRefresh();
		hintLoadingView();
	}

	private void hideSwipeRefresh() {
		if (mListener != null) mListener.showSwipeRefresh(false);
	}

	private void loadPoiSearchResult(List<PoiItem> items, int pageNum) {
		if (pageNum == 0)
			refreshListData(items);
		else if (!CommonUtil.isEmptyList(items))
			mListener.loadMoreListData(items);
	}

	private void refreshListData(List<PoiItem> items) {
		if (CommonUtil.isEmptyList(items))
			mListener.loadMoreListFail();
		else
			mListener.refreshListData(items);
	}

	//自定义考勤点搜索周边，如果默认1000范围内未搜索到相关地址，那么就扩大到全国搜索
	private boolean isExceedDistanceQueryPoiItem(List<PoiItem> items, int pageNum) {
		if (!CommonUtil.isEmptyList(items) || pageNum != 0 || mLocationType != K.location.LOCATION_CUSTOM_SEARCH)
			return false;
		if (customType == TYPE_CUSTOM_CITY) {
			isShowData = true;
			customType = TYPE_CUSTOM_COUNTRY;
			requestLoactionPoiItem();
		}
		else if (customType == TYPE_CUSTOM_COUNTRY) {
			customType = TYPE_CUSTOM_CITY;
			isShowData = false;
			sendMessageDelayed(pageNum);
		}
		return true;
	}

	private void sendMessageDelayed(int pageNum) {
		Message message = mHandler.obtainMessage();
		message.what = search_char_error;
		message.obj = pageNum;
		mHandler.sendMessageDelayed(message, 3000);
	}
}
