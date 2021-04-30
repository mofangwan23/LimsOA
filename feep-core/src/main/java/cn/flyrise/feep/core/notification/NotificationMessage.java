package cn.flyrise.feep.core.notification;

import java.io.Serializable;

public class NotificationMessage implements Serializable {

	private static final long serialVersionUID = -2304899633070888806L;
	private String msgId = "";
	private String id = "";
	private String type = "";
	private String userId = "";
	private String url = "";
	private int badge = 0;
	public String title;

	public int getBadge() {
		return badge;
	}

	public void setBadge(int badge) {
		this.badge = badge;
	}

	public String getMsgId() {
		return msgId;
	}

	public void seMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserI(String userId) {
		this.userId = userId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
