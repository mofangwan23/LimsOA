package cn.flyrise.feep.robot.util;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;

/**
 * 新建：陈冕;
 * 日期： 2017-8-14-17:39.
 * 天气类型
 */

public class RobotWeatherType {

	private static RobotWeatherType robotWeatherType;
	private final String[] nights = {"1", "2", "3", "13"};

	public static RobotWeatherType getInstance() {
		if (robotWeatherType == null) {
			robotWeatherType = new RobotWeatherType();
		}
		return robotWeatherType;
	}

	public int getWeatheIcon(String type, String data) {
		if (TextUtils.isEmpty(type)) {
			return R.drawable.weather_0;
		}

		String drawableName = String.format("weather_%s%s",
				CommonUtil.containsArray(type, nights) && DateUtil.isTimeNigh(data) ? "night_" : "", type);
		Context context = CoreZygote.getContext();
		return context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
	}

	public void onDestroy() {
		robotWeatherType = null;
	}

}
