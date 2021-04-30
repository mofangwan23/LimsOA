package com.hyphenate.chatui.retrieval;

/**
 * @author ZYP
 * @since 2018-05-04 15:23
 */
public class ChatMessage {

	public boolean isGroup;         // 是否群聊
	public String messageId;        // 只有一条相关记录才有
	public String conversationId;   // 会话id
	public String conversationName; // 会话名（单聊时为用户名）
	public String content;          // 内容
	public int imageRes;

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ChatMessage)) return false;

		ChatMessage that = (ChatMessage) o;

		return conversationId.equals(that.conversationId);
	}

	@Override public int hashCode() {
		return conversationId.hashCode();
	}
}
