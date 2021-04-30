/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����3:51:57
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.request.RequestContent;

public class CollaborationDetailsRequest extends RequestContent {

	public static final String NAMESPACE = "CollaborationDetailsRequest";

	private String id;
	private String requestType;
	private String msgId = "";

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getRequestType() {
		return CommonUtil.parseInt(requestType);
	}

	public void setRequestType(int requestType) {
		this.requestType = requestType + "";
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		if (msgId != null) {
			this.msgId = msgId;
		}
	}

}
