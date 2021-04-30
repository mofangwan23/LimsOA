package com.hyphenate.easeui.model;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import java.util.List;

/**
 * Created by klc on 2017/11/23.
 */

public class ChatMessageProvider {

	private EMConversation mConversation;
	private final int PAGE_COUNT = 20;
	private boolean hasMore;

	public ChatMessageProvider(EMConversation mConversation) {
		this.mConversation = mConversation;
	}

	public List<EMMessage> getInitMsg() {
		return loadMsgFormDB(null);
	}

	public List<EMMessage> getMemoryMsg() {
		return mConversation.getAllMessages();
	}

	public List<EMMessage> loadMoreMsg() {
		if (CommonUtil.isEmptyList(mConversation.getAllMessages())) {
			return loadMsgFormDB(null);
		}
		else {
			return loadMsgFormDB(mConversation.getAllMessages().get(0).getMsgId());
		}
	}

	private List<EMMessage> loadMsgFormDB(String msgID) {
		List<EMMessage> messageList = mConversation.loadMoreMsgFromDB(msgID, PAGE_COUNT);
		hasMore = PAGE_COUNT == messageList.size();
		return messageList;
	}

	public List<EMMessage> getMagToTarget(String messageID) {
		List<EMMessage> messageList = getMemoryMsg();
		while (getMsgPosition(messageList, messageID) == -1) {
			messageList = mConversation.loadMoreMsgFromDB(messageList.get(0).getMsgId(), PAGE_COUNT);
		}
		return mConversation.getAllMessages();
	}

	public int getMsgPosition(List<EMMessage> messages, String messgeID) {
		for (int i = 0; i < messages.size(); i++) {
			if (messages.get(i).getMsgId().equals(messgeID)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 清除内存中的数据
	 */
	public void clearMemoryMsg() {
		mConversation.clear();
	}

	public boolean isHasMore() {
		return hasMore;
	}
}
