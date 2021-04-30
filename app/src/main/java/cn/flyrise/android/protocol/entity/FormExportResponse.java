//
// FormSendDoChukouResponse.java
// feep
//
// Created by 钟永健 on 2013-3-21.
// Copyright 2012 flyrise. All rights reserved.
//
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * 送办处理：表单出口办理响应
 * @author 钟永健
 */
public class FormExportResponse extends ResponseContent {

	private String id;
	private String requestType;
	private String type;
	private String tableName;
	private String tableID;
	private String wfInfoID;
	@SerializedName("items")
	private List<ReferenceItem> referenceItems = new ArrayList<>();

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

	public List<ReferenceItem> getReferenceItems() {
		return referenceItems;
	}

	public void setReferenceItems(List<ReferenceItem> referenceItems) {
		this.referenceItems = referenceItems;
	}

}
