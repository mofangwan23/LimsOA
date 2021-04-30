package cn.flyrise.feep.robot.analysis;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.robot.entity.WhatCanSayItem;
import cn.flyrise.feep.robot.util.FileAssetsUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 新建：陈冕;
 * 日期： 2017-12-6-11:50.
 * 本地语音助手提示语句
 */

public class WhatCanSayAnalysis {

	private String whatCanSayData;
	private FileAssetsUtil mAssetsUtil;

	public WhatCanSayAnalysis(Context context) {
		if (mAssetsUtil == null) mAssetsUtil = new FileAssetsUtil();
		whatCanSayData = mAssetsUtil.getAssetsFileText(context, "cfg/robot_more.txt");
	}

	public List<WhatCanSayItem> analysis() {
		if (TextUtils.isEmpty(whatCanSayData)) {
			return null;
		}
		try {
			return getWhatCanSayItems();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<WhatCanSayItem> getWhatCanSayItems() throws JSONException {
		List<WhatCanSayItem> whatCanSayItems = new ArrayList<>();
		JSONArray jsonArray = new JSONArray(whatCanSayData);
		JSONObject item;
		WhatCanSayItem whatCanSayItem;
		for (int i = 0; i < jsonArray.length(); i++) {
			item = jsonArray.getJSONObject(i);
			if (item == null) {
				continue;
			}
			whatCanSayItem = new WhatCanSayItem();
			whatCanSayItem.id = item.has("id") ? item.getString("id") : "";
			whatCanSayItem.title = item.has("name") ? item.getString("name") : "";
			whatCanSayItem.content = item.has("alias") ? item.getString("alias") : "";
			whatCanSayItem.moduleId = item.has("moduleId") ? item.getInt("moduleId") : -1;
			whatCanSayItem.mores = getMores(item.has("more") ? item.getString("more") : ""
					, item.has("alias") ? item.getString("alias") : "");
			whatCanSayItems.add(whatCanSayItem);
		}
		return whatCanSayItems;
	}

	private List<String> getMores(String text, String alias) {
		List<String> mores = new ArrayList<>();
		mores.add(alias);
		if (text.contains("|")) {
			mores.addAll(Arrays.asList(text.split("\\|")));
		}
		else {
			mores.add(text);
		}
		return mores;
	}
}
