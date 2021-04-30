package cn.flyrise.feep.location.util;

import android.text.TextUtils;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * cm2017-4-21.
 * 自定义考勤数量
 */

public class LocationCustomSaveUtil {

	public static void setSavePoiItems(LocationSaveItem saveItem) {
		List<LocationSaveItem> items = new ArrayList<>();
		items.add(saveItem);
		setSavePoiItems(items);
	}

	//将改变后的数据，重新添加
	public static void setSavePoiItems(List<LocationSaveItem> saveItems) {
		Map<String, LocationSaveItem> poiItemMap = LocationSaveFileUtil.getInstance().getAddressCustom();
		if (poiItemMap == null) {
			poiItemMap = new HashMap<>();
		}
		else {
			poiItemMap.clear();//需要清空再添加
		}
		if (!CommonUtil.isEmptyList(saveItems)) {
			for (LocationSaveItem locationSaveItem : saveItems) {
				poiItemMap.put(locationSaveItem.poiId, locationSaveItem);
			}
		}
		LocationSaveFileUtil.getInstance().saveAddressCustom(poiItemMap);
	}

	//获取地图
	public static List<LocationSaveItem> getSavePoiItems() {
		Map<String, LocationSaveItem> poiItemMap = LocationSaveFileUtil.getInstance().getAddressCustom();
		if (poiItemMap == null) return null;
		List<LocationSaveItem> poiItems = new ArrayList<>();
		LocationSaveItem item;
		for (String key : poiItemMap.keySet()) {
			item = poiItemMap.get(key);
			if (item == null || TextUtils.isEmpty(item.poiId)) continue;
			poiItems.add(item);
		}
		return poiItems;
	}

	public static LocationSaveItem getSelectedLocationItem() {//获取当前选中的自定义考勤地点
		List<LocationSaveItem> locationSaves = getSavePoiItems();
		if (CommonUtil.isEmptyList(locationSaves)) return null;
		for (LocationSaveItem item : locationSaves) {
			if (item != null && item.isCheck) return item;
		}
		return null;
	}

	public static boolean isTempSelectedCustomAddress() { //当自定义考勤地址大于两个是，允许选择地址打卡
		List<LocationSaveItem> locationSaveItems = getSavePoiItems();
		return locationSaveItems != null && locationSaveItems.size() >= 2;
	}

	public static boolean isSavePoiItemNull() {
		return CommonUtil.isEmptyList(LocationCustomSaveUtil.getSavePoiItems());
	}

	public static boolean isSavePoiItemOnly() {
		return getSavePoiItems().size() == 1;
	}

	public static boolean isExistCheck() {
		LocationSaveItem item = LocationCustomSaveUtil.getSavePoiItems().get(0);
		return item != null && item.isCheck;
	}

	public static boolean isExistCustom() {//自定义签到是否有选中数据
		if (isSavePoiItemNull()) return false;
		if (isSavePoiItemOnly()) return isExistCheck(); //兼容6602以前的版本
		return getSelectedLocationItem() != null;
	}

}
