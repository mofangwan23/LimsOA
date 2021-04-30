//
// FormJiedian2SubnodeRequest.java
// feep
//
// Created by LuTH on 2012-2-8.
// Copyright 2012 flyrise. All rights reserved.
//
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.request.RequestContent;

public class FormSubnodeRequest extends RequestContent {

	public static final String NAMESPACE = "FormSubnodeRequest";

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

	private String requestType;
	private String id;
	private String type;
	private String tableName;
	private String tableID;
	private String wfInfoID;

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

	public int getType() {
		return CommonUtil.parseInt(type);
	}

	public void setType(int type) {
		this.type = type + "";
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableID() {
		return tableID;
	}

	public void setTableID(String tableID) {
		this.tableID = tableID;
	}

	public String getWfInfoID() {
		return wfInfoID;
	}

	public void setWfInfoID(String wfInfoID) {
		this.wfInfoID = wfInfoID;
	}

}
