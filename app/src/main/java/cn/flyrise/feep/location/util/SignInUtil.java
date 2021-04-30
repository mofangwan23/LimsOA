package cn.flyrise.feep.location.util;

import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import cn.flyrise.feep.location.bean.SignPoiItem;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import java.util.ArrayList;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-12-20-15:14.
 */

public class SignInUtil {

	//转换成适配器使用的类型
	public static List<SignPoiItem> poiItemsToSignPoiItem(List<PoiItem> poiItems) {
		List<SignPoiItem> signPoiItems = new ArrayList<>();
		if (CommonUtil.isEmptyList(poiItems)) return signPoiItems;
		SignPoiItem signPoiItem;
		for (PoiItem item : poiItems) {
			if (item == null) continue;
			signPoiItem = new SignPoiItem();
			signPoiItem.poiItem = item;
			signPoiItems.add(signPoiItem);
		}
		return signPoiItems;
	}

	public static float getExceedDistance(LatLng startLatLng, String latitude, String longitude, String outsign) {
		if (TextUtils.isEmpty(latitude) || TextUtils.isEmpty(longitude)) return 0;
		return getExceedDistance(startLatLng, setLatLng(latitude, longitude), outsign);
	}

	public static float getExceedDistance(LatLng startLatLng, LatLng endLatLng, String outsign) {
		try {
			return getExceedDistance(startLatLng, endLatLng, Integer.valueOf(outsign));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static LatLng setLatLng(String latitude, String longitude) {
		if (TextUtils.isEmpty(latitude) || TextUtils.isEmpty(longitude)) return null;
		try {
			return new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return null;
	}

	//判断考勤点坐标是否超出范围（小于等于0在考勤范围内）
	public static float getExceedDistance(LatLng startLatLng, LatLng endLatLng, int outsign) {
		if (startLatLng == null || endLatLng == null) return outsign;
		return (AMapUtils.calculateLineDistance(startLatLng, endLatLng)) - outsign;
	}

	public static float getExceedDistance(LatLng startLatLng, LocationSaveItem saveItem, int outsign) {
		return saveItem == null ? 0 : getExceedDistance(startLatLng, new LatLng(saveItem.Latitude, saveItem.Longitude), outsign);
	}

	public static List<PoiItem> signPoiItemToPoiItem(List<SignPoiItem> items) {
		if (CommonUtil.isEmptyList(items)) return null;
		List<PoiItem> poiItemList = new ArrayList<>();
		for (SignPoiItem item : items) {
			if (item == null) continue;
			poiItemList.add(item.poiItem);
		}
		return poiItemList;
	}

}
