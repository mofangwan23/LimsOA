package cn.flyrise.feep.robot.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.DateUtil;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 新建：陈冕;
 * 日期： 2017-10-12-16:23.
 */

public class RobotDateUtil {

	public static String scheduleEndTime(String time) {
		if (TextUtils.isEmpty(time)) {
			return time;
		}
		String dayAfter = "";
		try {
			String timeText = subTime(time);
			Date endDate = DateUtil.strToDate(timeText, "yyyy-MM-dd HH:mm:ss");
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(endDate);
			calendar.add(calendar.DATE, 1);//把日期往后增加一天.整数往后推,负数往前移动
			endDate = calendar.getTime();
			dayAfter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dayAfter;
	}

	public static String subTime(String date) {
		if (TextUtils.isEmpty(date)) {
			return date;
		}
		String dateTime = date;
		if (dateTime.contains("T")) {
			dateTime = dateTime.replace("T", " ");
		}
		if (dateTime.contains("AM")) {
			dateTime = dateTime.replace("AM", "");
		}
		if (dateTime.contains("PM")) {
			dateTime = dateTime.replace("PM", "");
		}
		return getDateTime(dateTime);
	}

	private static String getDateTime(String dateTime) {
		if (!isDateOrTimeNull(dateTime)) {
			return dateTime;
		}
		StringBuilder sb = new StringBuilder();
		if (isDateNull(dateTime)) {
			sb.append(getDate(isTomorrow(dateTime)));
		}

		if (isTimeNull(dateTime)) {
			sb.append("00:00:00");
		}
		else {
			sb.append(dateTime);
		}

		return sb.toString();
	}

	private static boolean isDateOrTimeNull(String strDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			sdf.parse(strDate);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private static boolean isDateNull(String strDate) {
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			sdf.parse(strDate);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private static boolean isTimeNull(String strTime) {
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm:ss");
		try {
			sdf.parse(strTime);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private static Calendar timeToCalendar(String strTime) {
		Calendar calendar = Calendar.getInstance();
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm:ss");
		try {
			calendar.setTime(sdf.parse(strTime));
			return calendar;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static boolean isTomorrow(String serviceTime) {//当前时间是否是明天
		Calendar service = timeToCalendar(serviceTime);
		if (service == null) return false;
		Calendar calendar = Calendar.getInstance();

		if (calendar.get(Calendar.HOUR_OF_DAY) != service.get(Calendar.HOUR_OF_DAY)) {
			return calendar.get(Calendar.HOUR_OF_DAY) > service.get(Calendar.HOUR_OF_DAY);
		}
		if (calendar.get(Calendar.MINUTE) != service.get(Calendar.MINUTE)) {
			return calendar.get(Calendar.MINUTE) > service.get(Calendar.MINUTE);
		}

		if (calendar.get(Calendar.SECOND) != service.get(Calendar.SECOND)) {
			return calendar.get(Calendar.SECOND) > service.get(Calendar.SECOND);
		}

		return true;
	}

	private static String getDate(boolean isTomorrow) {
		Calendar rightNow = Calendar.getInstance();
		if (isTomorrow) {
			rightNow.add(Calendar.DAY_OF_YEAR, 1);//日期加10天
		}
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return sdf.format(rightNow.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
