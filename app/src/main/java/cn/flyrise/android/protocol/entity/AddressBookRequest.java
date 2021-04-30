/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-4 ����04:42:03
 */

package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.request.RequestContent;

public class AddressBookRequest extends RequestContent {

	public static final String NAMESPACE = "AddressBookRequest";
	private String filterType;
	private String parentItemType;
	private String parentItemID;
	private String dataSourceType;
	private String page;
	private String perPageNums;
	private String orderBy;
	private String orderType;
	private String searchKey;
	private String searchUserID;
	private String isCurrentDept;
	private String currentDeptID;

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

	public int getFilterType() {
		return CommonUtil.parseInt(filterType);
	}

	public void setFilterType(int filterType) {
		this.filterType = filterType + "";
	}

	public int getParentItemType() {
		return CommonUtil.parseInt(parentItemType);
	}

	public void setParentItemType(int parentItemType) {
		this.parentItemType = parentItemType + "";
	}

	public String getParentItemID() {
		return parentItemID;
	}

	public void setParentItemID(String parentItemID) {
		this.parentItemID = parentItemID;
	}

	public int getDataSourceType() {
		return CommonUtil.parseInt(dataSourceType);
	}

	public void setDataSourceType(int dataSourceType) {
		this.dataSourceType = dataSourceType + "";
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

	public String getSearchUserID() {
		return searchUserID;
	}

	public void setSearchUserID(String searchUserID) {
		this.searchUserID = searchUserID;
	}

	public boolean isCurrentDept() {
		return !"0".equals(isCurrentDept);
	}

	public void setIsCurrentDept(boolean isCurrentDept) {
		this.isCurrentDept = isCurrentDept ? "1" : "0";
	}

	public String getCurrentDeptID() {
		return currentDeptID;
	}

	public void setCurrentDeptID(String currentDeptID) {
		this.currentDeptID = currentDeptID;
	}
}
