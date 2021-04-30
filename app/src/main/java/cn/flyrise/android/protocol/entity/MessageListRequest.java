package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class MessageListRequest extends RequestContent {

	private static final String NAMESPACE = "MessageListRequest";

	public static final int MESSAGETYPE_READ = 1;
	public static final int MESSAGETYPE_UNREAD = 2;

	/**
	 * 消息类型
	 */
	private String category;
	private String page;
	private String perPageNums;

	private String msgNum;

	private int messageType;     //给待阅已阅分类使用的 = =


	public void setPerPageNums(String perPageNums) {
		this.perPageNums = perPageNums;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public void setMsgNums(String msgNum) {
		this.msgNum   = msgNum;
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

}
