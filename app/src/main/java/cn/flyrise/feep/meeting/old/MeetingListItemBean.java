package cn.flyrise.feep.meeting.old;

import android.support.annotation.Keep;

@Keep
public class MeetingListItemBean {

	private String id;
	private String title;
	private String sendUser;
	private String time;
	private String status;
	private String startTime;
	private boolean isNews;
	private String endTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSendUser() {
		return sendUser;
	}

	public void setSendUser(String sendUser) {
		this.sendUser = sendUser;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public void setNews(boolean isNews) {
		this.isNews = isNews;
	}

	public boolean isNews() {
		return this.isNews;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getEndTime() {
		return endTime;
	}
}
