package cn.flyrise.feep.location.presenter;

import android.content.Context;
import cn.flyrise.feep.K;
import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.location.contract.LocationQueryPoiContract;
import cn.flyrise.feep.location.model.LocationQueryPoiItemModel;
import cn.flyrise.feep.location.util.SignInUtil;
import cn.flyrise.feep.location.views.LocationCustomSearchActivity;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-3-21-17:00.
 */

public class LocationCustomSearchPresenter implements FEListContract.Presenter
		, LocationQueryPoiItemModel.OnQueryPoiItemListener {

	public final static int TYPE = K.location.LOCATION_CUSTOM_SEARCH;
	private LocationQueryPoiContract mQueryPoiItem;
	private LocationCustomSearchActivity mView;
	private String searchKey;

	public LocationCustomSearchPresenter(Context context) {
		mView = (LocationCustomSearchActivity) context;
		mQueryPoiItem = new LocationQueryPoiItemModel(context, this);
	}

	@Override
	public void onStart() {

	}

	@Override
	public void refreshListData() {
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
		mQueryPoiItem.requestLoactionPoiItem();
	}

	@Override
	public void showSwipeRefresh(boolean isShow) {
		mView.showRefreshLoading(isShow);
	}

	@Override
	public void setEmptyingAdapter() {
//		mView.refreshListDatas(null, searchKey);
		mView.setCanPullUp(false);
	}

	@Override
	public void refreshListData(List<PoiItem> items) {
		mView.refreshListDatas(SignInUtil.poiItemsToSignPoiItem(items), searchKey);
		mView.setCanPullUp(hasMoreData());
	}

	@Override
	public void loadMoreListData(List<PoiItem> items) {
		mView.loadMoreListData(SignInUtil.poiItemsToSignPoiItem(items));
		mView.setCanPullUp(hasMoreData());
	}

	@Override
	public void loadMoreListFail() {
		mView.refreshListDatas(null, searchKey);
		mView.loadMoreListFail();
	}

	@Override
	public void loadMoreState(int state) {

	}
}
