package cn.flyrise.feep.study.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class GetQuestionRequest extends RequestContent {

	private String recordId = "2";

	private String requestType = "1";

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	@Override public String getNameSpace() {
		return "TrainingSignRequest";
	}

}
