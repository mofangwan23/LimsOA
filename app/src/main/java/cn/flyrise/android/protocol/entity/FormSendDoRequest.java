/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-21 上午11:37:38
 */
package cn.flyrise.android.protocol.entity;

import android.os.Parcel;
import android.os.Parcelable;
import cn.flyrise.android.protocol.model.FormNodeItem;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.request.RequestContent;
import java.util.ArrayList;
import java.util.List;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</               br> 修改时间：2013-3-21</br> 修改备注：</br>
 */
public class FormSendDoRequest extends RequestContent implements Parcelable {

	public static final String NAMESPACE = "FormSendDoRequest";

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

	private String requestType;
	private String id;
	private String currentFlowNodeGUID;
	private String dealType;
	private String suggestion;
	private String importance_key;
	private String importance_value;
	private String isTrace;
	private String isWait;
	private String isReturnCurrentNode;
	private String requiredData;

	// test 0719
	private String attachmentGUID;//存放手写签批图片GUID

	public String attachment;//存放普通附件上传的GUID

	public String getAttachmentGUID() {
		return attachmentGUID;
	}

	public void setAttachmentGUID(String attachmentGUID) {
		this.attachmentGUID = attachmentGUID;
	}

	// test 0719

	private List<FormNodeItem> nodes = new ArrayList<>();

	public int getRequestType() {
		return CommonUtil.parseInt(requestType);
	}

	public void setRequestType(int requestType) {
		this.requestType = requestType + "";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCurrentFlowNodeGUID() {
		return currentFlowNodeGUID;
	}

	public void setCurrentFlowNodeGUID(String currentFlowNodeGUID) {
		this.currentFlowNodeGUID = currentFlowNodeGUID;
	}

	public int getDealType() {
		return CommonUtil.parseInt(dealType);
	}

	public void setDealType(int dealType) {
		this.dealType = dealType + "";
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}

	public String getImportanceKey() {
		return importance_key;
	}

	public void setImportance_key(String importanceKey) {
		this.importance_key = importanceKey;
	}

	public String getImportance_value() {
		return importance_value;
	}

	public void setImportance_value(String importance_value) {
		this.importance_value = importance_value;
	}

	public boolean isTrace() {
		try {
			final int value = Integer.valueOf(isTrace);
			return value == 1;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setTrace(boolean isTrace) {
		this.isTrace = isTrace ? "1" : "0";
	}

	public boolean isWait() {
		try {
			final int value = Integer.valueOf(isWait);
			return value == 1;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setWait(boolean isWait) {
		this.isWait = isWait ? "1" : "0";
	}

	public boolean isReturnCurrentNode() {
		try {
			final int value = Integer.valueOf(isReturnCurrentNode);
			return value == 1;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setReturnCurrentNode(boolean isReturnCurrentNode) {
		this.isReturnCurrentNode = isReturnCurrentNode ? "1" : "0";
	}

	public List<FormNodeItem> getNodes() {
		return nodes;
	}

	public void setNodes(List<FormNodeItem> nodes) {
		this.nodes = nodes;
	}

	public void setRequiredData(String requiredData) {
		this.requiredData = requiredData;
	}

	public String getRequiredData() {
		return requiredData;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.requestType);
		dest.writeString(this.id);
		dest.writeString(this.currentFlowNodeGUID);
		dest.writeString(this.dealType);
		dest.writeString(this.suggestion);
		dest.writeString(this.importance_key);
		dest.writeString(this.importance_value);
		dest.writeString(this.isTrace);
		dest.writeString(this.isWait);
		dest.writeString(this.isReturnCurrentNode);
		dest.writeString(this.requiredData);
		dest.writeString(this.attachmentGUID);
	}

	public FormSendDoRequest() {
	}

	protected FormSendDoRequest(Parcel in) {
		this.requestType = in.readString();
		this.id = in.readString();
		this.currentFlowNodeGUID = in.readString();
		this.dealType = in.readString();
		this.suggestion = in.readString();
		this.importance_key = in.readString();
		this.importance_value = in.readString();
		this.isTrace = in.readString();
		this.isWait = in.readString();
		this.isReturnCurrentNode = in.readString();
		this.requiredData = in.readString();
		this.attachmentGUID = in.readString();
		this.nodes = new ArrayList<>();
	}

	public static final Parcelable.Creator<FormSendDoRequest> CREATOR = new Parcelable.Creator<FormSendDoRequest>() {
		@Override
		public FormSendDoRequest createFromParcel(Parcel source) {
			return new FormSendDoRequest(source);
		}

		@Override
		public FormSendDoRequest[] newArray(int size) {
			return new FormSendDoRequest[size];
		}
	};
}
