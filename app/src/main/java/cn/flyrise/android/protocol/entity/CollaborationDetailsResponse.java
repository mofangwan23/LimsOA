/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����11:48:20
 */
package cn.flyrise.android.protocol.entity;

import android.text.TextUtils;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.entry.AttachmentBean;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.protocol.model.Flow;
import cn.flyrise.android.protocol.model.Reply;
import cn.flyrise.android.protocol.model.SupplyContent;
import cn.flyrise.android.protocol.model.TrailContent;
import cn.flyrise.feep.core.network.request.ResponseContent;

public class CollaborationDetailsResponse extends ResponseContent {

	private String requestType;
	private String type;        // 0 协同; 1 表单
	private String id;
	private String title;
	private String sendUserID;
	private String sendUser;
	private String sendTime;
	private String content;
	private String currentFlowNodeName;

	@SerializedName("importance_key")
	private String importanceKey;
	@SerializedName("importance_value")
	private String importanceValue;

	private Flow flow;
	private String formFlowUrl;
	private String formHandleViewURL;
	private String isHasSubReplyAttachment;
	private String isCanReturnCurrentNode;
	private String attachmentGUID;
	private String isTrace;
	private String currentFlowNodeGUID;
	private String nodeName;
	private String nodeType;
	private String isAddsign;
	private String isReturn;

	private String needReply;
	private String mobileFormUrl;

	//6.6新加
	@SerializedName("isRepeating")
	private String canTransmit;  //允许转发
	private String isSendRead; //允许传阅
	private String isOver;
	public String favoriteId;
	public String taskId; // 2018/5/29 增加的，用作收藏请求的ID

	public String isEdit;//处理界面是否允许选择附件0：不允许，1：允许

	private List<SupplyContent> supplyContents = new ArrayList<>();
	private List<TrailContent> trailContents = new ArrayList<>();
	private List<AttachmentBean> attachments = new ArrayList<>();
	private List<Reply> replies;
	private List<Reply> originalReplies;

	@Override
	public String handle(JSONObject content) {
		try {
			// flow会出现为""（应该为{}）的情况，使用GSON会出错
			if (!content.isNull("query")) {
				final JSONObject queryContent = content.getJSONObject("query");
				if (!queryContent.isNull("flow")) {
					final String flowStr = queryContent.getString("flow");
					if ("".equals(flowStr)) {
						queryContent.put("flow", new JSONObject());
					}
				}
			}
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return content.toString();
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public int getType() {
		return CommonUtil.parseInt(type);
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSendUserID() {
		return sendUserID;
	}

	public void setSendUserID(String sendUserID) {
		this.sendUserID = sendUserID;
	}

	public String getSendUser() {
		return sendUser;
	}

	public void setSendUser(String sendUser) {
		this.sendUser = sendUser;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImportanceKey() {
		return importanceKey;
	}

	public void setImportanceKey(String importanceKey) {
		this.importanceKey = importanceKey;
	}

	public String getImportanceValue() {
		return importanceValue;
	}

	public void setImportanceValue(String importanceValue) {
		this.importanceValue = importanceValue;
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public String getFormFlowUrl() {
		return formFlowUrl;
	}

	public void setFormFlowUrl(String formFlowUrl) {
		this.formFlowUrl = formFlowUrl;
	}

	public String getFormHandleViewURL() {
		return formHandleViewURL;
	}

	public void setFormHandleViewURL(String formHandleViewURL) {
		this.formHandleViewURL = formHandleViewURL;
	}

	public boolean getIsHasSubReplyAttachment() {
		if ("1".equals(isHasSubReplyAttachment)) {
			return Boolean.TRUE;
		}
		else {
			return Boolean.FALSE;
		}
	}

	public void setIsHasSubReplyAttachment(String isHasSubReplyAttachment) {
		this.isHasSubReplyAttachment = isHasSubReplyAttachment;
	}

	public boolean getIsCanReturnCurrentNode() {
		if ("1".equals(isCanReturnCurrentNode)) {
			return Boolean.TRUE;
		}
		else {
			return Boolean.FALSE;
		}
	}

	public void setIsCanReturnCurrentNode(String isCanReturnCurrentNode) {
		this.isCanReturnCurrentNode = isCanReturnCurrentNode;
	}

	public String getAttachmentGUID() {
		return attachmentGUID;
	}

	public void setAttachmentGUID(String attachmentGUID) {
		this.attachmentGUID = attachmentGUID;
	}

	public boolean getIsTrace() {
		if ("1".equals(isTrace)) {
			return Boolean.TRUE;
		}
		else {
			return Boolean.FALSE;
		}
	}

	public void setIsTrace(String isTrace) {
		this.isTrace = isTrace;
	}

	public String getCurrentFlowNodeGUID() {
		return currentFlowNodeGUID;
	}

	public void setCurrentFlowNodeGUID(String currentFlowNodeGUID) {
		this.currentFlowNodeGUID = currentFlowNodeGUID;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public int getNodeType() {
		return CommonUtil.parseInt(nodeType);
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public boolean getIsAddsign() {
		return TextUtils.equals("1", isAddsign);
	}

	public void setIsAddsign(String isAddsign) {
		this.isAddsign = isAddsign;
	}

	public boolean getIsReturn() {
		if ("1".equals(isReturn)) {
			return Boolean.TRUE;
		}
		else {
			return Boolean.FALSE;
		}
	}

	public boolean getNeedReply() {
		if ("true".equals(needReply)) {
			return Boolean.TRUE;
		}
		else {
			return Boolean.FALSE;
		}
	}

	public void setIsReturn(String isReturn) {
		this.isReturn = isReturn;
	}

	public List<SupplyContent> getSupplyContents() {
		return supplyContents;
	}

	public void setSupplyContents(List<SupplyContent> supplyContents) {
		this.supplyContents = supplyContents;
	}

	public List<TrailContent> getTrailContents() {
		return trailContents;
	}

	public void setTrailContents(List<TrailContent> trailContents) {
		this.trailContents = trailContents;
	}

	public List<AttachmentBean> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentBean> attachments) {
		this.attachments = attachments;
	}

	public List<Reply> getReplies() {
		return replies;
	}

	public void setReplies(List<Reply> replies) {
		this.replies = replies;
	}

	public void addAttachments(List<AttachmentBean> attachments) {
		this.attachments.addAll(attachments);
	}

	public String getMobileFormUrl() {
		return mobileFormUrl;
	}

	public void setMobileFormUrl(String mobileFormUrl) {
		this.mobileFormUrl = mobileFormUrl;
	}


	public String getCurrentFlowNodeName() {
		return currentFlowNodeName;
	}

	public void setCurrentFlowNodeName(String currentFlowNodeName) {
		this.currentFlowNodeName = currentFlowNodeName;
	}

	public boolean isCanTransmit() {
		return canTransmit != null && "1".equals(canTransmit);
	}

	public boolean getIsSendRead() {
		return isSendRead != null && "1".equals(isSendRead);
	}

	public boolean isOver() {
		return isOver != null && "1".equals(isOver);
	}

	public List<Reply> getOriginalReplies() {
		return originalReplies;
	}
}
