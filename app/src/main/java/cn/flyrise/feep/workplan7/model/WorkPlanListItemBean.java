package cn.flyrise.feep.workplan7.model;

import android.support.annotation.Keep;
import com.google.gson.annotations.SerializedName;

/**
 * @author CWW
 * @version 1.0 <br/>
 * 创建时间: 2013-3-26 下午4:25:00 <br/>
 * 类说明 :
 */
@Keep
public class WorkPlanListItemBean {

	private String id;
	private String title;
	private String sendUser;
	private String sendTime;
	private String sectionName;
	@SerializedName("UserId")
	private String sendUserId;
	private String status;
	private String content;
	private String badge;

	private boolean isNews;

	private String type;//计划类型 1:日计划,2:周计划,3:月计划,4:其他计划

	public boolean isNews() {
		return isNews;
	}

	public String getBadge() {
		return badge;
	}

	public void setBadge(String badge) {
		this.badge = badge;
	}

	public void setNews(boolean news) {
		isNews = news;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

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

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSendUserId() {
		return sendUserId;
	}

	public void setSendUserId(String sendUserId) {
		this.sendUserId = sendUserId;
	}

	public String getType() {
		return type;
	}
}
