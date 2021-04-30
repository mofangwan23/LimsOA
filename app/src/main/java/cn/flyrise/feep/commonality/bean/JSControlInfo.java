/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-6 下午2:42:22
 */

package cn.flyrise.feep.commonality.bean;

import android.os.Parcel;
import android.os.Parcelable;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.form.been.PowersType;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</                                                               br> 修改时间：2012-9-6</br> 修改备注：</br>
 */
public class JSControlInfo implements Parcelable {

	private String uiControlType;

	private String uiControlId;

	@SerializedName("ControlDefaultData")
	private String controlDefaultData;

	private String attachmentGUID;

	private String actionType;

	@SerializedName("format")
	private String dataFormat;

	/**
	 * 非空性检查结果 true：仍存在必需值示输入，未通过检查 false： 已输入所有必需值
	 */
	private String isNull;

	private String formKeyId;

	private String meetingBoardURL;

	private List<ReferenceItem> referenceItems;

	private List<AddressBookItem> nodeItems;

	private String reportSearch;
	private String webData;

	private PowersType powersType;

	public List<JsSendServiceItem> sendService;//消息扩展，回传给js的参数

	public int getUiControlType() {
		return CommonUtil.parseInt(uiControlType);
	}

	public String getUiControlTypeValue() {
		return uiControlType;
	}

	public void setUiControlType(int uiControlType) {
		this.uiControlType = uiControlType + "";
	}

	public String getUiControlId() {
		return uiControlId;
	}

	public void setUiControlId(String uiControlId) {
		this.uiControlId = uiControlId;
	}

	public String getControlDefaultData() {
		return controlDefaultData;
	}

	public void setControlDefaultData(String controlDefaultData) {
		this.controlDefaultData = controlDefaultData;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public String getAttachmentGUID() {
		return attachmentGUID;
	}

	public void setAttachmentGUID(String attachmentGUID) {
		this.attachmentGUID = attachmentGUID;
	}

	public int getActionType() {
		return CommonUtil.parseInt(actionType);
	}

	public void setActionType(int actionType) {
		this.uiControlType = actionType + "";
	}

	public int getNullCheckResult() {
		return CommonUtil.parseInt(isNull);
	}

	public void setNullCheckResult(int nullCheckResult) {
		this.isNull = nullCheckResult + "";
	}

	public String getFormKeyId() {
		return formKeyId;
	}

	public void setFormKeyId(String formKeyId) {
		this.formKeyId = formKeyId;
	}

	public List<ReferenceItem> getReferenceItems() {
		return referenceItems;
	}

	public void setReferenceItems(List<ReferenceItem> referenceItems) {
		this.referenceItems = referenceItems;
	}

	public List<AddressBookItem> getNodeItems() {
		return nodeItems;
	}

	public void setNodeItems(List<AddressBookItem> nodeItems) {
		this.nodeItems = nodeItems;
	}

	public String getMeetingBoardURL() {
		return meetingBoardURL;
	}

	public void setMeetingBoardURL(String meetingBoardURL) {
		this.meetingBoardURL = meetingBoardURL;
	}

	public String getReportSearch() {
		return reportSearch;
	}

	public void setReportSearch(String reportSearch) {
		this.reportSearch = reportSearch;
	}

	public PowersType getPowersType() {
		return powersType;
	}

	public void setPowersType(PowersType powersType) {
		this.powersType = powersType;
	}

	/**
	 * 用于中间转换的接口，由于服务器端的JSON有时候会不标准，导致GSON会出现转换失败的情况，使用该回调接口可以将不标准的JSON内容转换为标准的JSON内容
	 * @param jsonStr 服务器端返回的JSON字符串
	 * @return 转换后，GSON可以处理的JSON内容
	 */
	public static String formatJsonString(String jsonStr) {
		try {
			final JSONObject properties = new JSONObject(jsonStr);
			// String referenceStr = properties.getString("referenceItems");
			final String referenceStr = properties.optString("referenceItems");
			if ("".equals(referenceStr)) {
				properties.put("referenceItems", new JSONArray());
			}
			final String nodeItemStr = properties.optString("nodeItems");
			if ("".equals(nodeItemStr)) {
				properties.put("nodeItems", new JSONArray());
			}
			final String powersTypeStr = properties.optString("powersType");
			if ("".equals(powersTypeStr)) {
				properties.put("powersType", new JSONObject());
			}
			return properties.toString();
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(actionType);
		dest.writeString(attachmentGUID);
		dest.writeString(controlDefaultData);
		dest.writeString(formKeyId);
		dest.writeString(meetingBoardURL);
		dest.writeList(nodeItems);
		dest.writeString(isNull);
		dest.writeList(referenceItems);
		dest.writeString(reportSearch);
		dest.writeString(uiControlId);
		dest.writeString(uiControlType);
		dest.writeParcelable(powersType, 0);
	}

	public void setWebData(String webData) {
		this.webData = webData;
	}

	public String getWebData() {
		return webData;
	}

	public static final Creator<JSControlInfo> CREATOR = new Creator<JSControlInfo>() {
		@SuppressWarnings("unchecked")
		@Override
		public JSControlInfo createFromParcel(Parcel source) {
			final JSControlInfo info = new JSControlInfo();
			info.actionType = source.readString();
			info.attachmentGUID = source.readString();
			info.controlDefaultData = source.readString();
			info.formKeyId = source.readString();
			info.meetingBoardURL = source.readString();
			info.nodeItems = source.readArrayList(AddressBookItem.class.getClassLoader());
			info.isNull = source.readString();
			info.referenceItems = source.readArrayList(ReferenceItem.class.getClassLoader());
			info.reportSearch = source.readString();
			info.uiControlId = source.readString();
			info.uiControlType = source.readString();
			info.powersType = source.readParcelable(PowersType.class.getClassLoader());
			return info;

		}

		@Override
		public JSControlInfo[] newArray(int size) {
			return null;
		}
	};
}
