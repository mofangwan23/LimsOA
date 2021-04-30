package cn.flyrise.android.protocol.entity.schedule;


import android.text.TextUtils;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by yj on 2016/7/13.
 */
public class AgendaResponseItem extends ResponseContent {

	public String id;
	public String title;
	public String startTime;
	public String eventSource;
	public String eventSourceId;
	@SerializedName("resId") public String shareUserId;
	@SerializedName("isSharedEvent") public String isSharedEvent;   // 1 为分享日程
	@SerializedName("detail") public String content;
	@SerializedName("awokeTime") public String promptTime;
	@SerializedName("stopTime") public String endTime;
	@SerializedName("period") public String loopRule;
	@SerializedName("meetingAtte") public String meetingId;

	@Override public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;

		AgendaResponseItem that = (AgendaResponseItem) object;

		if (!id.equals(that.id)) return false;
		return eventSourceId.equals(that.eventSourceId);

	}

	@Override public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + eventSourceId.hashCode();
		return result;
	}

	public int[] getDate() {
		try {
			int[] dates = new int[3];
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date date = sdf.parse(startTime);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			dates[0] = calendar.get(Calendar.YEAR);
			dates[1] = calendar.get(Calendar.MONTH) + 1;
			dates[2] = calendar.get(Calendar.DAY_OF_MONTH);
			return dates;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 是否是分享的日程
	 */
	public boolean isSharedEvent() {
		if (TextUtils.isEmpty(isSharedEvent)) {
			return false;
		}
		return TextUtils.equals(isSharedEvent, "1");
	}
}


