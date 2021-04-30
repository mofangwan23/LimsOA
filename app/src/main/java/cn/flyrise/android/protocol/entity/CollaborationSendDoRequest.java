/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-4 ����04:42:03
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.android.protocol.model.Flow;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.request.RequestContent;
import com.google.gson.annotations.SerializedName;

public class CollaborationSendDoRequest extends RequestContent {

	public static final String NAMESPACE = "CollaborationSendDoRequest";

	private String requestType;
	private String id;
	private String attitude;
	private String title;
	private String content;
	@SerializedName("importance_key")
	private String importanceKey;
	@SerializedName("importance_value")
	private String importanceValue;
	private String isTrace;
	private String isHidden;
	private String attachmentGUID;
	private Flow flow;
	// 是否可以更改正文
	private String isModify;
	private String deleteFile;

	//转发新加的:
	private String isChangeIdea;
	private String originalId;
	private String idea;

	//6.6新加的关联事项GUID
	private String relationFlow;

	//转发补丁新加的删除补充正文的关联事项ID；
	private String deleteRelationItem;

	//2018-3-8 ..协同退回增加【退回到发起人】、【重新提交后直接返回本节点】
	private boolean returnToStartNode;
	private boolean returnToThisNode;

	//2018-5-4 ..协同退回增加是否等待
	private String isWait;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAttitude() {
		return attitude;
	}

	public void setAttitude(String attitude) {
		this.attitude = attitude;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setImportanceKey(String importanceKey) {
		this.importanceKey = importanceKey;
	}

	public void setImportanceValue(String importanceValue) {
		this.importanceValue = importanceValue;
	}

	public void setIsTrace(boolean isTrace) {
		this.isTrace = isTrace ? "1" : "0";
	}

	public void setIsHidden(boolean isHidden) {
		this.isHidden = isHidden ? "1" : "0";
	}

	public void setIsWait(boolean isWait) {
		this.isWait = isWait ? "1" : "0";
	}

	public String getAttachmentGUID() {
		return attachmentGUID;
	}

	public void setAttachmentGUID(String attachmentGUID) {
		this.attachmentGUID = attachmentGUID;
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public void setDeleFile(String deleFile) {
		this.deleteFile = deleFile;
	}

	public static String getNAMESPACE() {
		return NAMESPACE;
	}

	public void setChangeIdea(String changeIdea) {
		isChangeIdea = changeIdea;
	}

	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}

	public void setIdea(String idea) {
		this.idea = idea;
	}

	public void setRelationFlow(String relationFlow) {
		this.relationFlow = relationFlow;
	}

	public void setDeleteRelationItem(String deleteRelationItem) {
		this.deleteRelationItem = deleteRelationItem;
	}

	public void setReturnToStartNode(boolean returnToStartNode) {
		this.returnToStartNode = returnToStartNode;
	}

	public void setReturnToThisNode(boolean returnToThisNode) {
		this.returnToThisNode = returnToThisNode;
	}
}
