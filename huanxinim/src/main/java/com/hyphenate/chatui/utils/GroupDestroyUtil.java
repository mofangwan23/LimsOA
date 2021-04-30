package com.hyphenate.chatui.utils;

import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.EaseUiK;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 新建：陈冕;
 * 日期： 2018-3-5-16:00.
 */

public class GroupDestroyUtil {

	public static List<EMMessage> getAllHistoryMessages(EMConversation conversation) { //获取聊天记录
		if (conversation == null) {
			return null;
		}
		List<EMMessage> messageList = conversation.getAllMessages();
		while (!CommonUtil.isEmptyList(messageList)) {
			messageList = conversation.loadMoreMsgFromDB(messageList.get(0).getMsgId(), 20);
		}
		return conversation.getAllMessages();
	}

	public static List<EMMessage> getFrontGroupDetroyHistoryMessages(EMConversation conversation) { //获取群聊解散前所有聊天记录
		return clearGroupDestroyMessage(getAllHistoryMessages(conversation));
	}

	private static List<EMMessage> clearGroupDestroyMessage(List<EMMessage> messageList) {//清理掉解散群聊后发送的消息
		if (CommonUtil.isEmptyList(messageList)) return messageList;
		List<EMMessage> removeMessge = new ArrayList<>();
		Collections.reverse(messageList);
		boolean isExistDestory = false;//是否存在解散群聊的消息
		for (EMMessage message : messageList) {
			if (message.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_SYSTEM, false)
					&& TextUtils.equals(GroupDestroyUtil.getBodyText(message)
					, CoreZygote.getContext().getString(R.string.group_destroy_msg))) {
				isExistDestory = true;
				break;
			}
			removeMessge.add(message);
		}
		Collections.reverse(messageList);
		if (CommonUtil.isEmptyList(removeMessge) || !isExistDestory) return messageList;
		for (EMMessage remove : removeMessge) {
			if (messageList.contains(remove)) messageList.remove(remove);
		}
		return messageList;
	}

	public static String getEmMessageText(List<EMMessage> emMessages) {//将聊天历史消息转为文本
		StringBuilder sb = new StringBuilder();
		String lastUserId = "";
		AddressBook addressBook = null;
		for (EMMessage msg : emMessages) {
			String userId = (msg.direct() == EMMessage.Direct.SEND) ? EMClient.getInstance().getCurrentUser() : msg.getFrom();
			if (!TextUtils.equals(lastUserId, userId)) {
				addressBook = CoreZygote.getAddressBookServices().queryUserInfo(userId);
			}
			sb.append(GroupDestroyUtil.getDateTimeOrText(msg.getMsgTime()) + "  ");
			if (addressBook != null) {
				sb.append(addressBook.name + "\n");
			}
			sb.append(getBodyText(msg) + "\n");
		}
		return sb.toString();
	}

	private static String getDateTimeOrText(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		try {
			return sdf.format(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private static String getDateTimeTitle(long time) {//写入到SDCard中的文件标题
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault());
		try {
			return sdf.format(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static long getStartTimeLong(List<EMMessage> emMessages) {//获取第一条数据的时间（创建群聊）
		return emMessages.get(0).getMsgTime();
	}

	public static long getEndTimeLong(List<EMMessage> emMessages) {//获取最后一条数据的时间（解散群聊）
		return emMessages.get(emMessages.size() > 0 ? (emMessages.size() - 1) : 0).getMsgTime();
	}

	public static String getStartTime(List<EMMessage> emMessages) {
		return getDateTimeTitle(getStartTimeLong(emMessages));
	}

	public static String getEndTime(List<EMMessage> emMessages) {
		return getDateTimeTitle(getEndTimeLong(emMessages));
	}

	public static String getBodyText(EMMessage message) {//输出的类型配置
		if (message.getType() == EMMessage.Type.TXT) {
			EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
			return txtBody.getMessage();
		}
		if (message.getType() == EMMessage.Type.IMAGE) {
			EMImageMessageBody imgBody = (EMImageMessageBody) message.getBody();
			return "音频";
		}
		if (message.getType() == EMMessage.Type.LOCATION) {
			EMLocationMessageBody locBody = (EMLocationMessageBody) message.getBody();
			return "地址";
		}
		if (message.getType() == EMMessage.Type.VOICE) {
			EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
			return "音频";
		}
		if (message.getType() == EMMessage.Type.VIDEO) {
			EMVideoMessageBody videoBody = (EMVideoMessageBody) message.getBody();
			return "视频";
		}
		if (message.getType() == EMMessage.Type.FILE) {
			EMNormalFileMessageBody body = (EMNormalFileMessageBody) message.getBody();
			return "文件";
		}
		return "";
	}

	public static String getTitle(String mGroupTitle) {//去除特殊字符
		if (TextUtils.isEmpty(mGroupTitle)) {
			return mGroupTitle;
		}
		if (mGroupTitle.contains("、")) {
			mGroupTitle = mGroupTitle.replaceAll("、", "_");
		}
		return mGroupTitle;
	}

}
