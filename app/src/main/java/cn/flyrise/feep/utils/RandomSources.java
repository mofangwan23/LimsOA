package cn.flyrise.feep.utils;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.flyrise.feep.R;

public class RandomSources {

	public static int getSourceById(String id) {
		int source = 0;
		if (TextUtils.isEmpty(id)) {
			return source;
		}

		long ran = System.currentTimeMillis();
		final int randomNumber = (int) (ran % 5);
		switch (randomNumber) {
			case 0:
				source = R.drawable.fe_listview_item_icon_bg_a;
				break;
			case 1:
				source = R.drawable.fe_listview_item_icon_bg_b;
				break;
			case 2:
				source = R.drawable.fe_listview_item_icon_bg_c;
				break;
			case 3:
				source = R.drawable.fe_listview_item_icon_bg_d;
				break;
			case 4:
				source = R.drawable.fe_listview_item_icon_bg_e;
				break;
			default:
				source = R.drawable.fe_listview_item_icon_bg_a;
				break;
		}
		return source;
	}
}
