/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-6 下午2:42:22
 */

package cn.flyrise.feep.form.been;

import android.support.annotation.Keep;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.core.common.utils.GsonUtil;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               br> 修改时间：2012-9-6</br> 修改备注：</br>
 */
@Keep
public class ExecuteResult {

	private String uiControlType;

	private String uiControlId;

	private String dateValue;

	private int attachmentCount;

	private String actionType;

	private List<ReferenceItem> referenceItems;

	private List<AddressBookItem> idItems;

	private MeetingBoardData meetingBoardData;

	private String data;

	public int getUiControlType() {
		return CommonUtil.parseInt(uiControlType);
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

	public String getDateValue() {
		return dateValue;
	}

	public void setDateValue(String dateValue) {
		this.dateValue = dateValue;
	}

	public int getAttachmentCount() {
		return attachmentCount;
	}

	public void setAttachmentCount(int attachmentCount) {
		this.attachmentCount = attachmentCount;
	}

	public int getActionType() {
		return CommonUtil.parseInt(actionType);
	}

	public void setActionType(int actionType) {
		this.actionType = actionType + "";
	}

	public List<ReferenceItem> getReferenceItems() {
		return referenceItems;
	}

	public void setReferenceItems(List<ReferenceItem> referenceItems) {
		this.referenceItems = referenceItems;
	}

	public List<AddressBookItem> getIdItems() {
		return idItems;
	}

	public void setIdItems(List<AddressBookItem> idItems) {
		this.idItems = idItems;
	}

	public MeetingBoardData getMeetingBoardData() {
		return meetingBoardData;
	}

	public void setMeetingBoardData(MeetingBoardData meetingBoardData) {
		this.meetingBoardData = meetingBoardData;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	/**
	 * 把本对象转换成json字符串
	 */
	public JSONObject getProperties() {
		final StringBuilder strBuf = new StringBuilder("{\"OcToJs_JSON\":");
		strBuf.append(GsonUtil.getInstance().toJson(this)).append("}");
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(strBuf.toString());
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	public String getJsMethod() {
		return "jsBridge.trigger('SetWebHTMLEditorContent',{\"OcToJs_JSON\":" + GsonUtil.getInstance().toJson(this) + "})";
	}

}
