package cn.flyrise.feep.meeting7.ui.bean;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.meeting7.selection.time.MeetingToolkitKt;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author ZYP
 * @since 2018-07-03 15:04
 * 被占用的会议室
 */
public class OccupyRoom {

	private static final SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String id;
	public String userId;
	public String topic;
	public int state;

	public int startYear, startMonth, startDay, startHour, startMinute;
	public int endYear, endMonth, endDay, endHour, endMinute;

	public void setDate(int year, int month, int day, int sHour, int sMinute, int eHour, int eMinute) {
		this.startYear = year;
		this.startMonth = month;
		this.startDay = day;
		this.startHour = sHour;
		this.startMinute = sMinute;

		this.endYear = year;
		this.endMonth = month;
		this.endDay = day;
		this.endHour = eHour;
		this.endMinute = eMinute;
	}

	public void setTime(int hour, int minute) {

		this.startHour = this.endHour = hour;
		this.startMinute = this.endMinute = minute;
	}

	public boolean isSameDay() {
		return startYear == endYear && startMonth == endMonth && startDay == endDay;
	}

	public boolean isThatDay(int year, int month, int day) {
		return startYear == year && startMonth == month && startDay == day;
	}

	public boolean isThatTime(int year, int month, int day, int hour, int minute) {
		if (startHour >= 24 || hour >= 24) return true;
		if (minute > 0 && minute < 30) {
			minute = 30;
		}
		else if (minute > 30) {
			minute = 0;
			hour += 1;
		}
		return this.startYear == year
				&& this.startMonth == month
				&& this.startDay == day
				&& this.startHour == hour
				&& this.startMinute == minute;
	}

	public OccupyRoom next() {
		OccupyRoom next = new OccupyRoom();
		next.id = id;
		next.topic = topic;
		next.userId = userId;

		next.startYear = startYear;
		next.startMonth = startMonth;
		next.startDay = startDay + 1;

		int monthDays = MeetingToolkitKt.getMonthDays(next.startYear, next.startMonth);
		if (next.startDay > monthDays) {    // 下月
			next.startDay = 1;
			next.startMonth = next.startMonth + 1;
		}

		if (next.startMonth > 11) {         // 下年
			next.startMonth = 0;
			next.startYear = next.startYear + 1;
		}

		next.endYear = next.startYear;
		next.endMonth = next.startMonth;
		next.endDay = next.startDay;

		next.startHour = 0;
		next.startMinute = 0;
		next.endHour = 23;
		next.endMinute = 0;
		return next;
	}

	public OccupyRoom nextTime() {
		OccupyRoom next = new OccupyRoom();
		next.id = id;
		next.topic = topic;
		next.userId = userId;

		next.startYear = next.endYear = startYear;
		next.startMonth = next.endMonth = startMonth;
		next.startDay = next.endDay = startDay;

		next.startHour = startHour;
		next.startMinute = startMinute + 30;
		if (next.startMinute == 60) {
			next.startMinute = 0;
			next.startHour = startHour + 1;
		}

		next.endHour = next.startHour;
		next.endMinute = next.startMinute;
		return next;
	}


	public String key() {
		return String.format("%d年%02d月%02d日", startYear, startMonth, startDay);
	}

	public String timeKey() {
		return String.format("%d年%02d月%02d日%02d时%02d分", startYear, startMonth, startDay, startHour, startMinute);
	}

	public OccupyRoom copy() {
		OccupyRoom or = new OccupyRoom();
		or.id = id;
		or.topic = topic;
		or.userId = userId;

		or.startYear = startYear;
		or.startMonth = startMonth;
		or.startDay = startDay;
		or.startHour = startHour;
		or.startMinute = startMinute;

		or.endYear = endYear;
		or.endMonth = endMonth;
		or.endDay = endDay;
		or.endHour = endHour;
		or.endMinute = endMinute;
		return or;
	}

	public static OccupyRoom newInstance(RoomUsage r) {
		OccupyRoom or = new OccupyRoom();
		or.id = r.id;
		or.topic = r.topics;
		or.userId = r.userId;

		Calendar startDate = null;
		Calendar endDate = null;

		try {
			startDate = Calendar.getInstance();
			startDate.setTime(sFormat.parse(r.startTime));
			endDate = Calendar.getInstance();
			endDate.setTime(sFormat.parse(r.endTime));
		} catch (Exception exception) {
			long startTime = CommonUtil.parseLong(r.startTime);
			long endTime = CommonUtil.parseLong(r.endTime);

			if (startTime > 0 && endTime > 0) {
				startDate = Calendar.getInstance();
				startDate.setTimeInMillis(startTime);

				endDate = Calendar.getInstance();
				endDate.setTimeInMillis(endTime);
			}
		}

		if (startDate != null && endDate != null) {
			or.startYear = startDate.get(Calendar.YEAR);
			or.startMonth = startDate.get(Calendar.MONTH);
			or.startDay = startDate.get(Calendar.DAY_OF_MONTH);
			or.startHour = startDate.get(Calendar.HOUR_OF_DAY);
			or.startMinute = startDate.get(Calendar.MINUTE);

			or.endYear = endDate.get(Calendar.YEAR);
			or.endMonth = endDate.get(Calendar.MONTH);
			or.endDay = endDate.get(Calendar.DAY_OF_MONTH);
			or.endHour = endDate.get(Calendar.HOUR_OF_DAY);
			or.endMinute = endDate.get(Calendar.MINUTE);
		}

		return or;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		OccupyRoom that = (OccupyRoom) o;

		if (!id.equals(that.id)) return false;
		if (!userId.equals(that.userId)) return false;
		return topic.equals(that.topic);
	}

	@Override public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + userId.hashCode();
		result = 31 * result + topic.hashCode();
		return result;
	}
}