package cn.flyrise.feep.location.model;

import android.text.TextUtils;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新建：陈冕;
 * 日期： 2018-5-5-16:11.
 */

public class LocationMonthCalendarModel {

	private List<Integer> exitSignDates = new ArrayList<>();//存在签到数据的日期
	private Map<Integer, List<FEListItem>> monthSignData = new HashMap<>();

	public void sqlistListItemData(List<FEListItem> listItems) {
		if (CommonUtil.isEmptyList(listItems)) return;
		int date;
		for (FEListItem item : listItems) {
			if (item == null) continue;
			date = getKey(item.getDate());
			if (date == -1) continue;
			if (!exitSignDates.contains(date)) exitSignDates.add(date);
			if (monthSignData.containsKey(date)) {
				List<FEListItem> feListItems = monthSignData.get(date);
				if (!CommonUtil.isEmptyList(feListItems) && !feListItems.contains(item)) feListItems.add(item);
			}
			else {
				List<FEListItem> items = new ArrayList<>();
				items.add(item);
				monthSignData.put(date, items);
			}
		}
		FELog.i("LocationSignCalendar", "-->>>>>LocationSign:" + listItems.size());
		FELog.i("LocationSignCalendar", "-->>>>>LocationSign:" + GsonUtil.getInstance().toJson(monthSignData));
	}

	public List<FEListItem> getFEListItem(int day) {
		if (monthSignData.size() <= 0 || !monthSignData.containsKey(day)) return null;
		return monthSignData.get(day);
	}

	public List<FEListItem> getFEListItem() {
		if (monthSignData.size() <= 0 || exitSignDates.size() <= 0) return null;
		return monthSignData.get(exitSignDates.get(0));
	}

	public List<FEListItem> getFEListItemDay(String date) {//date:2018.5.5
		if (monthSignData.size() <= 0 || !monthSignData.containsKey(getDayKey(date))) return null;
		return monthSignData.get(getDayKey(date));
	}

	public List<FEListItem> getFEListItemDays(String date) {//date:2018-5-5
		if (monthSignData.size() <= 0 || !monthSignData.containsKey(getKey(date))) return null;
		return monthSignData.get(getKey(date));
	}


	public List<Integer> getExitSignDates() {
		if (exitSignDates.size() <= 0) return null;
		return exitSignDates;
	}

	private int getKey(String date) {//date:2018-5-5
		if (TextUtils.isEmpty(date)) return -1;
		if (!date.contains("-")) return -1;
		String[] dates = date.split("-");
		if (dates.length != 3) return -1;
		return CommonUtil.parseInt(dates[2]);
	}


	private int getDayKey(String date) {//date:2018.5.5
		if (TextUtils.isEmpty(date) || !date.contains(".")) return -1;
		String[] texts = date.split("[.]");
		if (texts.length != 3) return -1;
		return CommonUtil.parseInt(texts[2]);
	}

	public void clear() {
		if (exitSignDates != null) exitSignDates.clear();
		if (monthSignData != null) monthSignData.clear();
	}

}
