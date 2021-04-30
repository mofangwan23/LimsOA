package cn.flyrise.feep.location.util;

import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * cm
 * 2017-4-21.
 * 存储搜索记录，在签到成功时进行保存
 */

public class LocationSearchRecordSaveUtil {

	private static final int maxNum = 5;  //存储数量

	public static void setSavePoiItem(LocationSaveItem item) {
		if (item == null) return;
		setSaveItem(item);
	}

	private static void setSaveItem(LocationSaveItem saveItem) {
		Map<String, LocationSaveItem> poiItemMap = LocationSaveFileUtil.getInstance().getAddressHint();
		if (poiItemMap == null) {
			poiItemMap = new HashMap<>();
		}
		if (poiItemMap.containsKey(saveItem.poiId)) {
			poiItemMap.remove(saveItem.poiId);
		}

		if (poiItemMap.size() >= maxNum) {
			deleteItem(poiItemMap);
		}
		poiItemMap.put(saveItem.poiId, saveItem);
		LocationSaveFileUtil.getInstance().saveAddressHint(poiItemMap);
	}

	//获取签到记录
	public static List<LocationSaveItem> getSavePoiItems() {
		Map<String, LocationSaveItem> poiItemMap = LocationSaveFileUtil.getInstance().getAddressHint();
		if (poiItemMap == null) {
			return null;
		}
		List<LocationSaveItem> poiItems = new ArrayList<>();
		LocationSaveItem item;
		for (String key : poiItemMap.keySet()) {
			item = poiItemMap.get(key);
			if (item == null) {
				continue;
			}
			if (TextUtils.isEmpty(item.poiId)) {
				continue;
			}
			poiItems.add(poiItemMap.get(key));
		}
		Collections.reverse(poiItems);
		return poiItems;
	}

	private static void deleteItem(Map<String, LocationSaveItem> poiItemMap) {
		int i = 1;
		List<String> keys = new ArrayList<>();
		LocationSaveItem saveItem;
		for (String key : poiItemMap.keySet()) {
			saveItem = poiItemMap.get(key);
			if (i == 1 || saveItem == null || TextUtils.isEmpty(saveItem.poiId)) {
				keys.add(key);
			}
			i++;
		}

		if (CommonUtil.isEmptyList(keys)) {
			return;
		}

		for (String key : keys) {
			if (poiItemMap.containsKey(key)) {
				poiItemMap.remove(key);
			}
		}

	}

	public static void deleteItem(String key) {
		Map<String, LocationSaveItem> map = LocationSaveFileUtil.getInstance().getAddressHint();
		if (map == null || !map.containsKey(key)) {
			return;
		}
		map.remove(key);
		LocationSaveFileUtil.getInstance().saveAddressHint(map);
	}

	public static void clear() {
		Map<String, LocationSaveItem> map = LocationSaveFileUtil.getInstance().getAddressHint();
		map.clear();
		LocationSaveFileUtil.getInstance().saveAddressHint(map);
	}
}
