/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-21 上午10:32:06
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.protocol.model.FormNodeItem;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</   br> 修改时间：2013-3-21</br> 修改备注：</br>
 */
public class FormNodeResponse extends ResponseContent {

	private String id;
	private String requestType;
	private String chukouID;
	private List<FormNodeItem> nodes = new ArrayList<>();

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

	public String getChukouID() {
		return chukouID;
	}

	public void setChukouID(String chukouID) {
		this.chukouID = chukouID;
	}

	public List<FormNodeItem> getNodes() {
		return nodes;
	}

	public void setNodes(List<FormNodeItem> nodes) {
		this.nodes = nodes;
	}

}
