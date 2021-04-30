/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-21 上午10:19:20
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @author Robert
 * @version 6.0.10<br>
 * 修改时间:2014-11-19<br>
 * 修改备注：新增一个字段RequiredData，主要用来将js返回的数据传到后台，实现条件流与pc端一样
 */
public class FormNodeRequest extends RequestContent {

	public static final String NAMESPACE = "FormNodeRequest";
	private String id;
	private String requestType;
	private String chukouID;
	private String checkedBtn;
	private String RequiredData;

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

	public String getChukouID() {
		return chukouID;
	}

	public void setChukouID(String chukouID) {
		this.chukouID = chukouID;
	}

	public String getCheckedBtn() {
		return checkedBtn;
	}

	public void setCheckedBtn(String checkedBtn) {
		this.checkedBtn = checkedBtn;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getRequiredData() {
		return RequiredData;
	}

	public void setRequiredData(String requiredData) {
		RequiredData = requiredData;
	}

}
