package cn.flyrise.feep.location.presenter;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.text.TextUtils;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.location.Sign.loadMore;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import cn.flyrise.feep.location.contract.LocationQueryPoiContract;
import cn.flyrise.feep.location.contract.LocationSendContract;
import cn.flyrise.feep.location.model.LocationQueryPoiItemModel;
import cn.flyrise.feep.location.util.SignInUtil;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.google.gson.reflect.TypeToken;
import java.util.List;

/**
 * cm 2017-3-14.
 */

public class LocationSendPresenter implements LocationSendContract.Presenter, LocationQueryPoiContract.OnQueryPoiItemListener {

	private int moveNum = 0;//判断是否为手势滑动

	private LocationSendContract.View mView;
	private Context mContext;
	private LocationQueryPoiContract mQueryPoiItem;

	public LocationSendPresenter(Context context) {
		mContext = context;
		mView = (LocationSendContract.View) context;
		mQueryPoiItem = new LocationQueryPoiItemModel(context, this);
	}

	@Override
	public void getGPSLocation() {
		mQueryPoiItem.getGPSLocation(mView.getLocationType());
	}

	@Override
	public void getMorePoiSearch() {
		mQueryPoiItem.loadMorePoiSearch();
	}

	@Override
	public void onCameraChange() {
		moveNum++;
	}

	@Override
	public void onCameraChangeFinish(CameraPosition cameraPosition) {
		if (!gpsIsOpen()) return;
		if (moveNum == 1) {
			moveNum = 0;
			return;
		}
		moveNum = 0;
		try {
			FELog.i("LocationSend", "-->>>>target-lat:" + cameraPosition.target.latitude);
			FELog.i("LocationSend", "-->>>>target-lon-:" + cameraPosition.target.longitude);
			mQueryPoiItem.requestLoactionPoiItem(new LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean gpsIsOpen() {
		LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	@Override
	public void gpsLocationSuccess(LatLng latlng) {//查看详情不用获取周边
		moveCurrentLocation(latlng);
		mQueryPoiItem.requestLoactionPoiItem();
	}

	@Override
	public void showSwipeRefresh(boolean isShow) {
		mView.swipeRefresh(isShow);
	}

	private void moveCurrentLocation(LatLng latLng) {
		mView.userCurrentPosition(latLng);
		mView.moveCurrentPosition(latLng);
	}

	@Override
	public void setEmptyingAdapter() {
		mView.loadMoreListFail();
	}

	@Override
	public void refreshListData(List<PoiItem> items) {
		if (CommonUtil.isEmptyList(items)) {
			mView.loadMoreListFail();
			return;
		}
		mView.refreshListData(SignInUtil.poiItemsToSignPoiItem(items));
	}

	@Override
	public void loadMoreListData(List<PoiItem> items) {
		mView.loadMoreListData(SignInUtil.poiItemsToSignPoiItem(items));
	}

	@Override
	public void loadMoreListFail() {
		mView.loadMoreListFail();
	}

	@Override
	public void loadMoreState(int state) {
		if (mView.getAdapter() == null) return;
		if (state == loadMore.can_load_more) {
			mView.getAdapter().setLoadState(BaseMessageRecyclerAdapter.LOADING);
		}
		else if (state == loadMore.no_load_more) {
			mView.getAdapter().setLoadState(BaseMessageRecyclerAdapter.LOADING_END);
		}
		else if (state == loadMore.success_load_more) {
			mView.getAdapter().setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE);
		}
	}

	@Override
	public LocationSaveItem poiItemToLocationSaveItem(PoiItem poiItem) {
		if (poiItem == null) return null;
		LocationSaveItem saveItem = new LocationSaveItem();
		saveItem.poiId = poiItem.getPoiId();
		saveItem.title = poiItem.getTitle();
		saveItem.content = poiItem.getCityName() + poiItem.getSnippet();
		saveItem.Latitude = poiItem.getLatLonPoint().getLatitude();
		saveItem.Longitude = poiItem.getLatLonPoint().getLongitude();
		return saveItem;
	}

	@Override
	public boolean isModifyRepeat(String poiId) {//考勤组添加判断是否重复
		if (((Activity) mContext).getIntent() == null) return false;
		String data = ((Activity) mContext).getIntent().getStringExtra(LocationSendContract.CUSTOM_POI_ITEMS);
		if (TextUtils.isEmpty(data)) return false;
		List<String> poiIds = GsonUtil.getInstance().fromJson(data, new TypeToken<List<String>>() {}.getType());
		return isModifyError(poiId, poiIds);
	}

	private boolean isModifyError(String poiId, List<String> poiIds) {
		if (CommonUtil.isEmptyList(poiIds)) return false;
		if (poiIds.contains(poiId)) {
			FEToast.showMessage(mContext.getResources().getString(R.string.location_custom_error));
			return true;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		mQueryPoiItem.destroyLocationGps();
		mQueryPoiItem = null;
	}
}
