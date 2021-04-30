package cn.flyrise.feep.location.bean;

import com.amap.api.maps.model.LatLng;

/**
 * Created by Administrator on 2017-4-21.
 */

public class LocationSaveItem {

	public String title;

	public String content;

	public double Latitude;

	public double Longitude;

	public String poiId;//地址唯一值

	public boolean isCheck;//自定义考勤点是否选中

	public LatLng getLatLng() {
		return new LatLng(Latitude, Longitude);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LocationSaveItem) {
			LocationSaveItem item = (LocationSaveItem) obj;
			return poiId.equals(item.poiId) && isCheck == item.isCheck;
		}
		return false;
	}
}
