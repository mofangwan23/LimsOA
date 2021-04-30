/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-21 上午9:33:47
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</               br> 修改时间：2013-3-21</br> 修改备注：</br>
 */
public class FormExportRequest extends RequestContent {

	public static final String NAMESPACE = "FormExportRequest";
	private String id;
	private String requestType;

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

}
