package cn.flyrise.feep.location.util;

import android.annotation.SuppressLint;
import cn.flyrise.feep.location.bean.LocationSignTime;
import java.util.Calendar;

/**
 * 新建：陈冕;
 * 日期： 2017-8-24-17:23.
 */

public class LocationSignDate {

	private boolean signTimeAdd = false;

	private static LocationSignDate instance;

	public static synchronized LocationSignDate getInstance() {
		if (instance == null) {
			instance = new LocationSignDate();
		}
		return instance;
	}

	@SuppressLint("DefaultLocale")
	public String getSignTime(LocationSignTime signData) {
		if (signTimeAdd = !signTimeAdd) {
			return String.format("%02d : %02d", signData.hour, signData.minute);
		}
		else {
			return String.format("%02d   %02d", signData.hour, signData.minute);
		}
	}

	@SuppressLint("DefaultLocale")
	public static String getServiceTime(LocationSignTime signData) {
		if (signData == null) {
			return "";
		}
		return String.format("%02d:%02d:%02d", signData.hour, signData.minute, signData.second);
	}

	@SuppressLint("DefaultLocale")
	static LocationSignTime subSignData(Calendar calendar) {
		if (calendar == null) {
			return null;
		}
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		LocationSignTime signData = new LocationSignTime();
		signData.data = String.format("%02d-%02d-%02d", year, month, day);
		signData.hour = hour;
		signData.minute = minute;
		signData.second = second;
		return signData;
	}

}
