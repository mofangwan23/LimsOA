//
// PushMessage.java
// feep
//
// Created by LuTH on 2011-11-30.
// Copyright 2011 flyrise. All rights reserved.
//
package cn.flyrise.android.protocol.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class NotificationMessageItem implements Serializable {

	private static final long serialVersionUID = -2704899633050888806L;
	// androidpn
	private String notificationId;
	private String apikey;
	private String title;
	private String message;                                 // json
	private String url;

	//
	private String badge;
	private String messageID;
	private int messageType;

	public String getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBadge() {
		return badge;
	}

	public void setBadge(String badge) {
		this.badge = badge;
	}

	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public NotificationMessageItem() {

	}

	// 组包
	public void getProperties(JSONObject properties) {

	}

	public void setProperties(String jsonStr) throws Exception {
		final JSONObject properties = new JSONObject(jsonStr);
		setProperties(properties);

	}

	/*
	 * {"aps":{ "alert":"林总下达招聘通知,林总下达招聘通知,林总下达招聘通知", "sound":"beep.wav", "badge":"114" }, "json":"{ "iq":{ "namespace":"PushMessageResponse", "query":{ "id":"6", "messageType":"3" } } }" }
	 */
	// 解包
	public void setProperties(JSONObject properties) {
		if (properties != null) {

			JSONObject aps = new JSONObject();
			JSONObject query = new JSONObject();

			try {
				aps = findProperty(properties, "aps");
			} catch (final JSONException e) {
				e.printStackTrace();
			}

			try {
				query = findProperty(properties, "json/iq/query");

			} catch (final JSONException e) {
				e.printStackTrace();
			}

			try {
				this.setTitle(aps.getString("alert"));
			} catch (final JSONException e) {
				e.printStackTrace();
			}

			try {
				this.setBadge(aps.getString("badge"));
			} catch (final JSONException e) {
				e.printStackTrace();
			}

			try {
				this.setMessageID(query.getString("id"));
			} catch (final JSONException e) {
				e.printStackTrace();
			}

			try {
				this.setMessageType(query.getInt("messageType")); // 0
			} catch (final JSONException e) {
				e.printStackTrace();
			}

		}
	}

	public static JSONObject findProperty(JSONObject properties, String path) throws JSONException {
		return findProperty(properties, path, "");
	}

	// + (id) findProperty: (NSDictionary *) properties path:(NSString *)path
	// namespace:(NSString *)namespace;
	public static JSONObject findProperty(JSONObject properties, String path, String namespace) throws JSONException {
		final String[] chunks = path.split("/");
		JSONObject currentElement = properties;

		JSONObject valueObject;
		final int len = chunks.length;
		for (final String chunk : chunks) {
			try {
				valueObject = currentElement.getJSONObject(chunk);
			} catch (final JSONException e) {
				// e.printStackTrace();
				currentElement.put(chunk, new JSONObject());
				currentElement = currentElement.getJSONObject(chunk);
				break;
			}
			currentElement = valueObject;
		}

		return currentElement;
	}
}
