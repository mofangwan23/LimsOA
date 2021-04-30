package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.request.RequestContent;

public class SendReplyRequest extends RequestContent {

	public static final String NAMESPACE = "SendReplyRequest";

	private String id;
	private String replyID;
	private String attachmentGUID;
	private String isSendMsg = "0";
	private String content;
	private String replyType;
	public String client = "Android";

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

	public String getReplyID() {
		return replyID;
	}

	public void setReplyID(String replyID) {
		this.replyID = replyID;
	}

	public String getAttachmentGUID() {
		return attachmentGUID;
	}

	public void setAttachmentGUID(String attachmentGUID) {
		this.attachmentGUID = attachmentGUID;
	}

	public String getIsSendMsg() {
		return isSendMsg;
	}

	public void setIsSendMsg(String isSendMsg) {
		this.isSendMsg = isSendMsg;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getReplyType() {
		return CommonUtil.parseInt(replyType);
	}

	public void setReplyType(int replyType) {
		this.replyType = replyType + "";
	}
}
