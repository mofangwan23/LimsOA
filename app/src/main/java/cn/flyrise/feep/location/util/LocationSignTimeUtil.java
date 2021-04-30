package cn.flyrise.feep.location.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import cn.flyrise.feep.R;

/**
 * 新建：陈冕;
 * 日期： 2017-11-21-18:18.
 * 05:30~10:59 上午
 * 11:00~15:29 中午
 * 15:30~18:29 下午
 * 18:30~05:29 晚上
 */

public class LocationSignTimeUtil {

	private static final String time1 = "05:30";
	private static final String time2 = "11:00";
	private static final String time3 = "15:30";
	private static final String time4 = "18:30";
	private static final String time5 = "23:59";
	private static final String time6 = "00:00";

	public static int getSignTimeDrawable(String serviceTime) {
		if (isTimeExist(getTime(serviceTime), getTime(time1), getTime(time2))) {//早上
			return R.drawable.location_sign_success_morning;
		}
		else if (isTimeExist(getTime(serviceTime), getTime(time2), getTime(time3))) { //中午
			return R.drawable.location_sign_success_noon;
		}
		else if (isTimeExist(getTime(serviceTime), getTime(time3), getTime(time4))) { //下午
			return R.drawable.location_sign_success_afternoon;
		}
		else if (isTimeNightExist(getTime(serviceTime), getTime(time4), getTime(time5))
				|| isTimeExist(getTime(serviceTime), getTime(time6), getTime(time1))) { //晚上
			return R.drawable.location_sign_success_night;
		}
		return R.drawable.location_sign_success_morning;
	}

	private static boolean isTimeExist(long curTime, long start, long end) {
		return curTime >= start && curTime < end;
	}

	//凌晨12点校验
	private static boolean isTimeNightExist(long curTime, long start, long end) {
		return curTime >= start && curTime <= end;
	}

	private static long getTime(String time) {
		SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
		try {
			return sf.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
