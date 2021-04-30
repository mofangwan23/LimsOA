package com.hyphenate.easeui.utils;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.utils.GroupDestroyUtil;
import com.hyphenate.easeui.EaseUiK;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by klc on 2017/12/21.
 */

public class MMPMessageUtil {

	/**
	 * 创建一个撤回消息
	 */
	public static void saveRecallMessage(Context context, EMMessage emMessage, boolean initiative) {
		if (initiative) {
			sendMessage(context.getString(R.string.recall_msg_self), emMessage, initiative);
		}
		else {
			CoreZygote.getAddressBookServices().queryUserDetail(emMessage.getFrom())
					.subscribe(f -> {
						String messageStr = String.format(context.getString(R.string.recall_msg_other), f != null ? f.name : "");
						sendMessage(messageStr, emMessage, initiative);
					}, error -> {
						sendMessage(String.format(context.getString(R.string.recall_msg_other), ""), emMessage, initiative);
					});
		}
	}

	private static void sendMessage(String message, EMMessage emMessage, boolean initiative) {
		String to;
		if (emMessage.getChatType() == EMMessage.ChatType.Chat && !initiative) {
			to = emMessage.getFrom();
		}
		else {
			to = emMessage.getTo();
		}
		EMMessage msg = createSystemMsg(message, emMessage.getChatType(), to, emMessage.getMsgTime());
		EMClient.getInstance().chatManager().saveMessage(msg);
	}


	/**
	 * 创建一个被退群或者被移除群聊消息
	 */
	public static void saveRemoveGroupMessage(Context context, String groupID, boolean initiative) {
		if (EMClient.getInstance().getOptions().isDeleteMessagesAsExitGroup()) {
			return;
		}
		String messageStr;
		if (initiative) {
			messageStr = context.getString(R.string.remove_group_self);
		}
		else {
			messageStr = context.getString(R.string.remove_group_other);
		}
		EMMessage msg = createSystemMsg(messageStr, EMMessage.ChatType.GroupChat, groupID, -1);
		EMClient.getInstance().chatManager().saveMessage(msg);
	}


	/**
	 * 创建一个群组解散的消息
	 */
	public static void saveGroupDestroyMessage(Context context, String groupID) {
		if (EMClient.getInstance().getOptions().isDeleteMessagesAsExitGroup()) {
			return;
		}
		String messageStr = context.getString(R.string.group_destroy_msg);
		EMMessage msg = createSystemMsg(messageStr, EMMessage.ChatType.GroupChat, groupID, getGroupEndMessageTiem(groupID));
		EMClient.getInstance().chatManager().saveMessage(msg);
	}

	/**
	 * 创建一个用户加群的消息
	 */
	private static void saveLocalInviteMsgForGroup(String groupID, String[] userIDs) {
		String msgText = "你邀请了" + getInviteUser(userIDs) + "加入群聊";
		EMMessage msg = createSystemMsg(msgText, EMMessage.ChatType.GroupChat, groupID, -1);
		EMClient.getInstance().chatManager().saveMessage(msg);
	}

	public static void saveLocalInviteMsgForGroup(EMMessage cmdMsg) {
		String inviterUserID = cmdMsg.getFrom();
		AddressBook inviterUser = CoreZygote.getAddressBookServices().queryUserInfo(inviterUserID);
		if (inviterUser != null) {
			inviterUserID = inviterUser.name;
		}
		String userStr = cmdMsg.getStringAttribute(EaseUiK.EmChatContent.CMD_ADD_USERLIST, "");
		userStr = userStr.replace(CoreZygote.getAddressBookServices().getActualUserId(EMClient.getInstance().getCurrentUser()) + ",", "");
		String[] addUsers = userStr.split(",");
		if (addUsers.length == 0) return;
		String msgText = inviterUserID + "邀请了" + getInviteUser(addUsers) + "加入群聊";
		EMMessage msg = createSystemMsg(msgText, EMMessage.ChatType.GroupChat, cmdMsg.getTo(), -1);
		EMClient.getInstance().chatManager().saveMessage(msg);
	}

	private static String getInviteUser(String[] addUsers) {
		int length = addUsers.length >= 30 ? 30 : addUsers.length;
		StringBuilder messageStr = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (!TextUtils.equals(CoreZygote.getLoginUserServices().getUserId(), addUsers[i])) {
				AddressBook addressBook = CoreZygote.getAddressBookServices().queryUserInfo(addUsers[i]);
				if (addressBook != null)
					messageStr.append(addressBook.name).append("、");
			}
		}
		messageStr.deleteCharAt(messageStr.length() - 1);
		return messageStr.toString();
	}

	/**
	 * 创建一个清除聊天记录的消息
	 */
	public static void saveClearHistoryMsg(String chatId, boolean isGroup, long time) {
		String messageStr = CoreZygote.getContext().getString(R.string.history_has_been_empited_tip);
		EMMessage msg = createSystemMsg(messageStr, isGroup ? EMMessage.ChatType.GroupChat : EMMessage.ChatType.Chat, chatId, time);
		msg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_NOT_SHOW_CONTENT, true);
		EMClient.getInstance().chatManager().saveMessage(msg);
	}

	/**
	 * 某某退出群聊
	 */
	public static void memberExitedMsg(String chatId, String memberId) {
		CoreZygote.getAddressBookServices().queryUserDetail(memberId)
				.subscribe(memberDetail -> {
					EMMessage msg = createSystemMsg(String.format("%s 退出了该群", memberDetail.name), EMMessage.ChatType.GroupChat, chatId, -1);
					EMClient.getInstance().chatManager().saveMessage(msg);
				}, error -> {

				});
	}


	/**
	 * 创建本地的语音通话记录消息
	 */
	public static void saveCallMsg(boolean voiceCall, boolean isSend, String userID, long msgTime, String txtBody) {
		EMMessage message;
		if (isSend) {
			message = EMMessage.createSendMessage(EMMessage.Type.TXT);
			message.setTo(userID);
		}
		else {
			message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
			message.setDirection(EMMessage.Direct.RECEIVE);
			message.setFrom(userID);
		}
		if (msgTime != -1) {
			message.setMsgTime(msgTime);
		}
		message.setAttribute(
				voiceCall ? EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VOICE_CALL : EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VIDEO_CALL, true);
		message.addBody(new EMTextMessageBody(txtBody));
		message.setStatus(EMMessage.Status.SUCCESS);
		EMClient.getInstance().chatManager().saveMessage(message);
	}

	private static EMMessage createSystemMsg(String message, EMMessage.ChatType chatType, String to, long time) {
		EMMessage emMessage = EMMessage.createSendMessage(EMMessage.Type.TXT);
		EMTextMessageBody txtBody = new EMTextMessageBody(message);
		emMessage.addBody(txtBody);
		emMessage.setFrom(EMClient.getInstance().getCurrentUser());
		emMessage.setTo(to);
		emMessage.setUnread(false);
		emMessage.setChatType(chatType);
		if (time != -1) {
			emMessage.setMsgTime(time);
		}
		emMessage.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_SYSTEM, true);
		return emMessage;
	}

	private static long getGroupEndMessageTiem(String groupId) {//获取群聊的最后一天消息时间
		if (TextUtils.isEmpty(groupId)) return -1;
		EMConversation writerConversation = EMClient.getInstance().chatManager().getConversation(groupId);
		if (writerConversation == null) return -1;
		List<EMMessage> messages = GroupDestroyUtil.getAllHistoryMessages(writerConversation);
		if (CommonUtil.isEmptyList(messages)) return -1;
		return messages.get(messages.size() - 1).getMsgTime() + 1000;
	}

	/****************************************** 发送网络消息******************************************/

	/**
	 * 群聊加人邀请CMD消息
	 */
	public static void sendInviteMsg(String groupID, List<AddressBook> addressBooks) {
		EMMessage inviteMsg = EMMessage.createSendMessage(EMMessage.Type.TXT);
		int length = addressBooks.size() > 20 ? 20 : addressBooks.size();
		StringBuilder messageStr = new StringBuilder();
		messageStr.append(CoreZygote.getLoginUserServices().getUserName()).append("邀请了");
		for (int i = 0; i < length; i++) {
			if (!addressBooks.get(i).userId.equals(CoreZygote.getLoginUserServices().getUserId())) {
				messageStr.append(addressBooks.get(i).name).append("、");
			}
		}
		messageStr.deleteCharAt(messageStr.length() - 1);
		if (length < addressBooks.size()) {
			messageStr.append("等人");
		}
		messageStr.append("加入了群聊");
		EMTextMessageBody body = new EMTextMessageBody(messageStr.toString());
		inviteMsg.addBody(body);
		inviteMsg.setTo(groupID);
		inviteMsg.setChatType(EMMessage.ChatType.GroupChat);
		inviteMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_SYSTEM, true);
		EMClient.getInstance().chatManager().sendMessage(inviteMsg);
	}
}
