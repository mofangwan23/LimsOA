package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2018-07-03 09:36
 */
public class SubReplyRequest extends RequestContent {

	public static final String NAMESPACE = "SendReplyRequest";

	public String id;
	public String replyID;
	public String attachmentGUID;
	public String isSendMsg;
	public String content;
	public String replyType;
	public String client = "Android";

	public SubReplyRequest() {
		this.replyType = "1";
		this.isSendMsg = "0";
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}
}
