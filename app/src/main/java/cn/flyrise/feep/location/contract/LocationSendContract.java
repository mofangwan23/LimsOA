package cn.flyrise.feep.location.contract;

import cn.flyrise.feep.location.adapter.LocationSendReportAdapter;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import cn.flyrise.feep.location.bean.SignPoiItem;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import java.util.List;

/**
 * cm2017-4-19.
 */

public interface LocationSendContract {

	String CUSTOM_POI_ITEMS = "requst_poi_ids";//自定义界面已存在的poiId

	String CUSTOM_IS_MODIFY = "custom_is_modify";//是否为修改

	interface Presenter {

		void getGPSLocation();

		void getMorePoiSearch();

		void onCameraChange();

		void onCameraChangeFinish(CameraPosition cameraPosition);//滑动结束之后返回地址

		boolean gpsIsOpen();

		boolean isModifyRepeat(String key);//考勤组添加判断是否重复

		LocationSaveItem poiItemToLocationSaveItem(PoiItem poiItem);

		void onDestroy();
	}

	interface View {

		void userCurrentPosition(LatLng latLng);//当前位置

		void moveCurrentPosition(LatLng latlng);//移动到当前位置

		void setMarkerAnimation();//图标的动画

		void swipeRefresh(boolean isRefresh);//是否显示刷新的图标

		void refreshListData(List<SignPoiItem> items);

		void loadMoreListData(List<SignPoiItem> items);

		void loadMoreListFail();

		int getLocationType();

		LocationSendReportAdapter getAdapter();
	}

}
