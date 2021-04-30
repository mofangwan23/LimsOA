/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-4 ����04:42:03
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.request.RequestContent;

public class ListRequest extends RequestContent {

	public static final String NAMESPACE = "ListRequest";

	private String requestType;
	private String page;
	private String perPageNums;
	private String orderBy;
	private String orderType;
	private String searchKey;
	private String id;
	private String userId;                    // 新增在v6.03 2015-3-27

	private int sumId;//月汇总详情类型0或不填返回所有记录，102 休息天数,103 迟到,104 早退,105 缺卡,106 旷工,107 外勤,108 未签到

	private String lastId;//防止数据不刷新引起的问题

	public String getLastMessageId() {
		return lastId;
	}

	public void setLastMessageId(String lastMessageId) {
		this.lastId = lastMessageId;
	}

	public int getSumId() {
		return sumId;
	}

	public void setSumId(int sumId) {
		this.sumId = sumId;
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

	public int getRequestType() {
		return CommonUtil.parseInt(requestType);
	}

	public void setRequestType(int requestType) {
		this.requestType = requestType + "";
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getPerPageNums() {
		return perPageNums;
	}

	public void setPerPageNums(String perPageNums) {
		this.perPageNums = perPageNums;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
