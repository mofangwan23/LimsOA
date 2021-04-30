package cn.flyrise.feep.study.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class TrainingTaskRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "TrainingTaskRequest";
	}

	private String userId;

	private String recordId;

	private String page;

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	private String requestType;

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
