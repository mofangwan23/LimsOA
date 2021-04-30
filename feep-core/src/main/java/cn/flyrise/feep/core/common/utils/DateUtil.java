package cn.flyrise.feep.core.common.utils;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.R;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author ZYP
 * @since 2017-02-24 15:42
 */
public class DateUtil {

	private static long serviceTime;
	private static long setServiceLocalTime;

	private static SimpleDateFormat sdfEnglish;
	private static SimpleDateFormat sdfChina1;
	private static SimpleDateFormat sdfChina2;
	private static SimpleDateFormat sdfChina3;

	/**
	 * 保存服务器的时间，计算一个时间差
	 * @param serviceTime 服务器时间
	 */
	public static void setServiceTime(long serviceTime) {
		DateUtil.serviceTime = serviceTime;
		DateUtil.setServiceLocalTime = System.currentTimeMillis();
	}

	public static long getServiceTime() {
		return serviceTime + (System.currentTimeMillis() - setServiceLocalTime);
	}


	/**
	 * 把时间转换成 00:00格式
	 * @param time 时间
	 */
	public static String formatTime(int time) {
		final int minute = time / 60;
		final int second = time % 60;
		return String.format(Locale.getDefault(), "%02d:%02d", minute, second);
	}

	/**
	 * 将string转换为时间
	 */
	public static Calendar str2Calendar(String strDate) {
		Calendar calendar = Calendar.getInstance();
		if (TextUtils.isEmpty(strDate))
			return calendar;
		String value = null;
		if (strDate.length() >= 4)
			calendar.set(Calendar.YEAR, Integer.valueOf(strDate.substring(0, 4)));
		if (strDate.length() >= 7 && !(value = strDate.substring(5, 7)).equals("00"))
			calendar.set(Calendar.MONTH, Integer.valueOf(value) - 1);
		if (strDate.length() >= 10 && !(value = strDate.substring(8, 10)).equals("00"))
			calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(value));
		if (strDate.length() >= 13) {
			value = strDate.substring(11, 13);
			calendar.set(Calendar.HOUR_OF_DAY, CommonUtil.parseInt(value));
		}
		if (strDate.length() >= 16)
			value = strDate.substring(14, 16);
		calendar.set(Calendar.MINUTE, CommonUtil.parseInt(value));
		return calendar;
	}

	public static Calendar str2Calendar(String strDate, String timeFormat) {
		Date date = strToDate(strDate, timeFormat);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	public static Date stringToDateTime(String text) {//转换可能是外国的日期格式
		if (sdfEnglish == null) sdfEnglish = new SimpleDateFormat("MM dd yyyy hh:mmaa", Locale.ENGLISH);
		if (sdfChina1 == null) sdfChina1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		if (sdfChina2 == null) sdfChina2 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
		if (sdfChina3 == null) sdfChina3 = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
		Date dateTime = null;
		try {
			dateTime = sdfChina1.parse(text);
		} catch (ParseException e) {
			e.printStackTrace();
			try {
				dateTime = sdfEnglish.parse(text);
			} catch (ParseException e1) {
				e1.printStackTrace();
				try {
					dateTime = sdfChina2.parse(text);
				} catch (ParseException e2) {
					e2.printStackTrace();
					try {
						dateTime = sdfChina3.parse(text);
					} catch (ParseException e3) {
						e3.printStackTrace();
					}
				}
			}
		}
		return dateTime;
	}


	/**
	 * 将string转换为时间
	 */
	public static Date strToDate(String strDate, String pattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.getDefault());
		ParsePosition pos = new ParsePosition(0);
		return formatter.parse(strDate, pos);
	}


	/**
	 * 截取出日期
	 */
	public static String strToString(String dateStr) {
		if (TextUtils.isEmpty(dateStr) && dateStr.length() < 11) {
			return dateStr;
		}
		StringBuilder str = new StringBuilder(dateStr);
		return str.substring(0, 10);
	}

	public static String subDatBirthday(Calendar calendar) {
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		return String.format("%02d-%02d-%02d", year, month, day);
	}

	public static String subDateYYYYMMDD(Context context, Calendar calendar) {
		return (String) DateFormat.format(context.getResources().getString(R.string.util_date_yyyy_mm_dd), calendar);
	}

	public static String subDateMMDD(Context context, Calendar calendar) {
		return (String) DateFormat.format(context.getResources().getString(R.string.util_date_interval), calendar);
	}


	public static String subDateY(Calendar calendar) {
		return String.valueOf(calendar.get(Calendar.YEAR));
	}

	public static String subDatehm(Calendar calendar) {
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		return String.format("%02d:%02d", hour, minute);
	}

	public static String dateIntervalNum(Calendar calendar1, Calendar calendar2) {
		long day;
		Date startDate = calendar1.getTime();
		Date endData = calendar2.getTime();
		day = (endData.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000) + 1L;
		return String.valueOf(day);
	}

	/**
	 * 获取日期为某月的第几周
	 */
	public static String getMMWeek(Context context, String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		Calendar calendar = Calendar.getInstance();
		Date date;
		try {
			date = sdf.parse(str);
		} catch (Exception e) {
			sdf = new SimpleDateFormat("MM dd yyyy", Locale.getDefault());
			try {
				date = sdf.parse(str);
			} catch (Exception ex) {
				return "";
			}
		}
		calendar.setTime(date);
		return DateFormat.format(context.getResources().getString(R.string.util_date_mm), date.getTime())
				+ String.format(context.getResources().getString(R.string.util_date_week), calendar.get(Calendar.WEEK_OF_MONTH));

	}


	public static boolean isTimeout(String time) {
		if (TextUtils.isEmpty(time)) {
			return false;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		ParsePosition pos = new ParsePosition(0);
		try {
			Date date = formatter.parse(time, pos);
			return date.getTime() < System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String calendar2StringDate(Calendar calendar) {
		if (calendar == null) return "";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		return formatter.format(calendar.getTime());
	}

	public static String calendar2StringDateTime(Calendar calendar) {
		if (calendar == null) return "";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		return formatter.format(calendar.getTime());
	}


	public static int getTimeLevelForFormat(String format) {
		if (TextUtils.isEmpty(format)) return 5;
		if (format.contains("NN")) return 5;
		if (format.contains("HH")) return 4;
		if (format.contains("DD")) return 3;
		if (format.contains("MM")) return 2;
		if (format.contains("YYYY")) return 1;
		return 5;
	}

	//18点至凌晨5点为晚上（24小时制）
	public static boolean isTimeNigh(String strDate) {
		if (TextUtils.isEmpty(strDate)) {
			return false;
		}
		Calendar mCalendar = str2Calendar(strDate);
		if (mCalendar == null) {
			return false;
		}
		int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
		return hour >= 18 || hour <= 5;
	}

	/**
	 * 获取日期为周几
	 */
	public static String getDayOfWeek(Context context, String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		Calendar calendar = Calendar.getInstance();
		Date date;
		try {
			date = sdf.parse(str);
		} catch (Exception e) {
			sdf = new SimpleDateFormat("MM dd yyyy", Locale.getDefault());
			try {
				date = sdf.parse(str);
			} catch (Exception ex) {
				return "";
			}
		}
		calendar.setTime(date);
		return calendarForWeek(context, calendar);
	}

	public static String calendarForWeek(Context context, Calendar calendar) {
		String mWay = String.valueOf(calendar.get(Calendar.DAY_OF_WEEK));
		if ("1".equals(mWay)) {
			return context.getResources().getString(R.string.util_week_sun);
		}
		else if ("2".equals(mWay)) {
			return context.getResources().getString(R.string.util_week_mon);
		}
		else if ("3".equals(mWay)) {
			return context.getResources().getString(R.string.util_week_tues);
		}
		else if ("4".equals(mWay)) {
			return context.getResources().getString(R.string.util_week_wed);
		}
		else if ("5".equals(mWay)) {
			return context.getResources().getString(R.string.util_week_thur);
		}
		else if ("6".equals(mWay)) {
			return context.getResources().getString(R.string.util_week_fri);
		}
		else if ("7".equals(mWay)) {
			return context.getResources().getString(R.string.util_week_sat);
		}
		return "";
	}


	public static String formatTimeForList(String strTime) {
		if (TextUtils.isEmpty(strTime)) return "";
		SimpleDateFormat sdf;
		if (strTime.length() < 11) {
			sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		}
		else {
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		}
		try {
			Date date = sdf.parse(strTime);
			long time = date.getTime();
			if (isToday(time)) {
				if (strTime.length() < 11)
					return CoreZygote.getContext().getString(R.string.time_format_today);
				else {
					return formatTime(time, CoreZygote.getContext().getString(R.string.time_format_Hm));
				}
			}
			else if (isYesterday(time))
				if (strTime.length() < 11)
					return CoreZygote.getContext().getString(R.string.time_format_yesterday);
				else {
					return CoreZygote.getContext().getString(R.string.time_format_yesterday) + " " + formatTime(time,
							CoreZygote.getContext().getString(R.string.time_format_Hm));
				}
			else if (isThisYear(time)) {
				return formatTime(time, CoreZygote.getContext().getString(R.string.time_format_Md));
			}
			else {
				return formatTime(time, CoreZygote.getContext().getString(R.string.time_format_yMd));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return strTime;
	}

	public static String formatTimeForList(long time) {
		if (isToday(time)) {
			return formatTime(time, CoreZygote.getContext().getString(R.string.time_format_Hm));
		}
		else if (isYesterday(time)) {
			return CoreZygote.getContext().getString(R.string.time_format_yesterday);
		}
		else if (isThisYear(time)) {
			return formatTime(time, CoreZygote.getContext().getString(R.string.time_format_Md));
		}
		else {
			return formatTime(time, CoreZygote.getContext().getString(R.string.time_format_yMd));
		}
	}

	public static String formatTimeForDetail(String strTime) {
		if (TextUtils.isEmpty(strTime)) return "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		try {
			Date date = sdf.parse(strTime);
			long time = date.getTime();
			return formatTimeForDetail(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return strTime;
	}

	public static String formatTimeForDetail(long time) {
		if (isToday(time)) {
			return formatTime(time, CoreZygote.getContext().getString(R.string.time_format_Hm));
		}
		else if (isYesterday(time)) {
			return CoreZygote.getContext().getString(R.string.time_format_yesterday) + " " + formatTime(time,
					CoreZygote.getContext().getString(R.string.time_format_Hm));
		}
		else if (isThisYear(time)) {
			return formatTime(time, CoreZygote.getContext().getString(R.string.time_format_Mdhm));
		}
		else {
			return formatTime(time, CoreZygote.getContext().getString(R.string.time_format_yMdhm));
		}
	}

	public static String formatTimeToHm(String time) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
			Date date;
			date = sdf.parse(time);
			return formatTime(date.getTime(), CoreZygote.getContext().getString(R.string.time_format_Hm));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}

	public static String formatTimeForHms(long time) {
		return formatTime(time, "yyyy-MM-dd HH:mm:ss");
	}


	public static String formatTime(long time, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(time);
	}


	private static boolean isToday(long time) {
		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		Calendar end = Calendar.getInstance();
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		end.set(Calendar.MILLISECOND, 999);
		return time >= start.getTimeInMillis() && time <= end.getTimeInMillis();
	}


	private static boolean isYesterday(long time) {
		Calendar start = Calendar.getInstance();
		start.add(Calendar.DATE, -1);
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		Calendar end = Calendar.getInstance();
		end.add(Calendar.DATE, -1);
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		end.set(Calendar.MILLISECOND, 999);
		return time >= start.getTimeInMillis() && time <= end.getTimeInMillis();
	}

	private static boolean isThisYear(long time) {
		Calendar start = Calendar.getInstance();
		start.set(Calendar.MONTH, 0);
		start.set(Calendar.DAY_OF_MONTH, 1);
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		Calendar end = Calendar.getInstance();
		end.set(Calendar.MONTH, 11);
		end.set(Calendar.DAY_OF_MONTH, 31);
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		end.set(Calendar.MILLISECOND, 999);
		return time >= start.getTimeInMillis() && time <= end.getTimeInMillis();
	}
}
