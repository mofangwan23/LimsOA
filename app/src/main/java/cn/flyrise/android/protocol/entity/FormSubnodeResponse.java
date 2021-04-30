//
// FormJiedian2SubnodeResponse.java
// feep
//
// Created by LuTH on 2012-2-8.
// Copyright 2012 flyrise. All rights reserved.
//
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * 送办处理：节点到人/岗位 响应
 * @author LuTH
 */
public class FormSubnodeResponse extends ResponseContent {

	private String id;
	private String type;
	private List<ReferenceItem> items = new ArrayList<>();

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

	public List<ReferenceItem> getItems() {
		return items;
	}

	public void setItems(List<ReferenceItem> items) {
		this.items = items;
	}

}
