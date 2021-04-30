package cn.flyrise.feep.meeting7.ui.bean;

import android.os.Parcel;
import android.os.Parcelable;
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem;
import cn.flyrise.feep.meeting7.selection.bean.MSTimeItem;
import java.util.Calendar;

/**
 * @author ZYP
 * @since 2018-06-29 15:25
 */
public class RoomInfo implements Parcelable {

	public String roomId;
	public String roomName;

	private int type;    // 会议时间类型：跨天(1)、当天(0)

	// 开始
	public int startYear, startMonth, startDay, startHour, startMinute;

	// 结束
	public int endYear, endMonth, endDay, endHour, endMinute;

	public RoomInfo() { }

	protected RoomInfo(Parcel in) {
		roomId = in.readString();
		roomName = in.readString();
		type = in.readInt();
		startYear = in.readInt();
		startMonth = in.readInt();
		startDay = in.readInt();
		startHour = in.readInt();
		startMinute = in.readInt();
		endYear = in.readInt();
		endMonth = in.readInt();
		endDay = in.readInt();
		endHour = in.readInt();
		endMinute = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(roomId);
		dest.writeString(roomName);
		dest.writeInt(type);
		dest.writeInt(startYear);
		dest.writeInt(startMonth);
		dest.writeInt(startDay);
		dest.writeInt(startHour);
		dest.writeInt(startMinute);
		dest.writeInt(endYear);
		dest.writeInt(endMonth);
		dest.writeInt(endDay);
		dest.writeInt(endHour);
		dest.writeInt(endMinute);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<RoomInfo> CREATOR = new Creator<RoomInfo>() {
		@Override
		public RoomInfo createFromParcel(Parcel in) {
			return new RoomInfo(in);
		}

		@Override
		public RoomInfo[] newArray(int size) {
			return new RoomInfo[size];
		}
	};

	public boolean hasSelectedDate() {
		// && startMonth != 0 && endMonth != 0 月份这里是可以等于 0 的
		return startYear != 0 && endYear != 0 && startDay != 0 && endDay != 0;

	}

	public MSDateItem startDate() {
		if (!hasSelectedDate()) {
			return null;
		}

		if (type == 0) {
			// TODO 仅在开发阶段
//			if (startMinute < 30) {
//				startMinute = 0;
//			}
//			else if (startMinute > 30) {
//				startMinute = 30;
//			}

			MSTimeItem item = new MSTimeItem();
			item.setYear(startYear);
			item.setMonth(startMonth);
			item.setDay(startDay);
			item.setHour(startHour);
			item.setMinute(startMinute);
			return item;
		}

		MSDateItem item = new MSDateItem();
		item.setYear(startYear);
		item.setMonth(startMonth);
		item.setDay(startDay);
		return item;
	}

	public MSDateItem endDate() {
		if (!hasSelectedDate()) {
			return null;
		}

		if (type == 0) {

			// TODO 仅在开发阶段
//			if (endMinute < 30 && endMinute > 0) {
//				endMinute = 30;
//			}
//			else if (endMinute > 30) {
//				endMinute = 0;
//				endHour += 1;
//			}

			MSTimeItem item = new MSTimeItem();
			item.setYear(endYear);
			item.setMonth(endMonth);
			item.setDay(endDay);
			item.setHour(endHour);
			item.setMinute(endMinute);
			return item;
		}

		MSDateItem item = new MSDateItem();
		item.setYear(endYear);
		item.setMonth(endMonth);
		item.setDay(endDay);
		return item;
	}

	public void updateInfo(MSDateItem startDate, MSDateItem endDate) {
		if (startDate != null) {
			this.startYear = startDate.getYear();
			this.startMonth = startDate.getMonth();
			this.startDay = startDate.getDay();

			if (startDate instanceof MSTimeItem) {
				MSTimeItem t = (MSTimeItem) startDate;
				this.startHour = t.getHour();
				this.startMinute = t.getMinute();
			}
		}
		else {
			this.startYear = 0;
			this.startMonth = 0;
			this.startDay = 0;
			this.startHour = 0;
			this.startMinute = 0;
		}

		if (endDate != null) {
			this.endYear = endDate.getYear();
			this.endMonth = endDate.getMonth();
			this.endDay = endDate.getDay();

			if (endDate instanceof MSTimeItem) {
				MSTimeItem t = (MSTimeItem) endDate;
				this.endHour = t.getHour();
				this.endMinute = t.getMinute();
			}
		}
		else {
			this.endYear = 0;
			this.endMonth = 0;
			this.endDay = 0;
			this.endHour = 0;
			this.endMinute = 0;
		}
	}

	public int getType() {
		return this.type;
	}

	public void setType(int type) {
		if (this.type != type) {
			this.startYear = 0;
			this.startMonth = 0;
			this.startDay = 0;
			this.startHour = 0;
			this.startMinute = 0;
			this.endYear = 0;
			this.endMonth = 0;
			this.endDay = 0;
			this.endHour = 0;
			this.endMinute = 0;
		}
		this.type = type;
	}

	public void onDateChange(int year, int month, int day) {
		if (this.type == 0) {
			this.startYear = this.endYear = year;
			this.startMonth = this.endMonth = month;
			this.startDay = this.endDay = day;
			this.startHour = this.endHour = this.startMinute = this.endMinute = 0;
		}
	}

	public boolean isTimeValidate() {
		Calendar start = Calendar.getInstance();
		start.set(Calendar.YEAR, startYear);
		start.set(Calendar.MONTH, startMonth);
		start.set(Calendar.DAY_OF_MONTH, startDay);
		start.set(Calendar.HOUR_OF_DAY, startHour);
		start.set(Calendar.SECOND, startMinute);

		Calendar end = Calendar.getInstance();
		end.set(Calendar.YEAR, endYear);
		end.set(Calendar.MONTH, endMonth);
		end.set(Calendar.DAY_OF_MONTH, endDay);
		end.set(Calendar.HOUR_OF_DAY, endHour);
		end.set(Calendar.SECOND, endMinute);
		return start.before(end);
	}

	public boolean isStartTimeStale() {
		Calendar startTIme = Calendar.getInstance();
		startTIme.set(Calendar.YEAR, startYear);
		startTIme.set(Calendar.MONTH, startMonth);
		startTIme.set(Calendar.DAY_OF_MONTH, startDay);
		startTIme.set(Calendar.HOUR_OF_DAY, startHour);
		startTIme.set(Calendar.MINUTE, startMinute);

		Calendar currentTime = Calendar.getInstance();
		return startTIme.before(currentTime);
	}

}
