package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 * 创建时间: 2013-3-12 下午3:18:14 <br/>
 * 类说明 :
 */
public class NewsDetailsRequest extends RequestContent {

	private static final String NAMESPACE = "NewsDetailsRequest";

	private String requestType;                     // 5.新闻、6.公告
	private String id;
	private String msgId;

	public NewsDetailsRequest(String id, int requestType, String msgID) {
		this.id = id;
		this.requestType = requestType + "";
		this.msgId = msgID == null ? "" : msgID;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public static String getNamespace() {
		return NAMESPACE;
	}

}
