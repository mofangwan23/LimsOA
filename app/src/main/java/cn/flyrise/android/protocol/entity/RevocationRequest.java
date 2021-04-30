/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-4 ����04:42:03
 */
package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.RequestContent;

public class RevocationRequest extends RequestContent {

	public static final String NAMESPACE = "RemoteRequest";

	private String obj;
	private String method;
	private String count;
	@SerializedName("param1")
	private String id;

	public RevocationRequest(String id, int type) {
		this.id = id;
		this.count = "1";
		if (type == 0) {
			this.method = "deleteCollaborative";
		}
		else {
			this.method = "deleteWorkflow";
		}
		this.obj = "taskService";
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}


}
