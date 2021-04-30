package com.hyphenate.chatui.retrieval;

import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMConversation.EMSearchDirection;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-04 14:36
 * 这个鬼东西专门来搜历史记录的
 * 性能问题不用想，百分百有的
 */
public class ChatMessagesRepository {

	/**
	 * 根据关键字查询聊天记录
	 * @param keyword 关键字
	 * @param maxSize 查询多少条数据，-1 表示查询所有
	 */
	public Observable<List<ChatMessage>> queryMessage(String keyword, int maxSize) {
		return Observable.unsafeCreate(f -> {
			if (!IMHuanXinHelper.getInstance().isImLogin()) {
				f.onError(new RuntimeException("Hyphenate doesn't login.!"));
				f.onCompleted();
				return;
			}

			Collection<EMConversation> conversationValues = EMClient.getInstance().chatManager().getAllConversations().values();
			if (CommonUtil.isEmptyList(conversationValues)) {
				f.onError(new NullPointerException("Empty messages."));
				f.onCompleted();
				return;
			}

			List<EMConversation> conversations = new ArrayList<>(conversationValues);
			Collections.sort(conversations, conversationSort);

			List<ChatMessage> results = maxSize > 0 ? new ArrayList<>(maxSize) : new ArrayList<>();

			for (EMConversation conversation : conversations) {
				if (maxSize > 0 && results.size() == maxSize) break;
				List<EMMessage> messages = conversation.searchMsgFromDB(
						keyword, System.currentTimeMillis(), 999, null, EMSearchDirection.UP);

				if (CommonUtil.isEmptyList(messages)) continue;                 // 讲道理，这个逻辑应该是不会出现的才对

				// name、conversationId、extra
				ChatMessage chatMessage = new ChatMessage();
				chatMessage.isGroup = conversation.isGroup();
				chatMessage.conversationId = conversation.conversationId();

				if (chatMessage.isGroup) {
					EMGroup group = EMClient.getInstance().groupManager().getGroup(chatMessage.conversationId);
					chatMessage.conversationName = group == null
							? CoreZygote.getConvSTServices().getCoversationName(chatMessage.conversationId)
							: group.getGroupName();
					chatMessage.imageRes = R.drawable.em_group_icon;
				}
				else {
					CoreZygote.getAddressBookServices().queryUserDetail(chatMessage.conversationId)
							.subscribe(addressBook -> {
								if (addressBook == null) {
									chatMessage.imageRes = R.drawable.em_ic_admin;
									chatMessage.conversationName = chatMessage.conversationId;
								}
								else {
									chatMessage.conversationName = addressBook.name;
								}
							}, error -> {
								chatMessage.imageRes = R.drawable.em_ic_admin;
								chatMessage.conversationName = chatMessage.conversationId;
							});
				}

				if (messages.size() == 1) {
					EMMessage message = messages.get(0);
					if (!(message.getBody() instanceof EMTextMessageBody)) continue;    // 只搞文本消息
					if (message.getBooleanAttribute("is_system", false)) continue;      // 过滤掉系统消息

					chatMessage.messageId = message.getMsgId();
					chatMessage.content = ((EMTextMessageBody) message.getBody()).getMessage();
				}
				else {
					chatMessage.content = messages.size() + "条相关记录";
				}

				results.add(chatMessage);
			}

			if (CommonUtil.isEmptyList(results)) {
				f.onError(new RuntimeException("Empty chat message."));
				f.onCompleted();
				return;
			}

			f.onNext(results);
			f.onCompleted();
		});
	}

	private Comparator conversationSort = (Comparator<EMConversation>) (lhs, rhs) -> {
		boolean lhsTop = !TextUtils.isEmpty(lhs.getExtField());
		boolean rhsTop = !TextUtils.isEmpty(rhs.getExtField());
		if (lhsTop && !rhsTop) {
			return -1;
		}
		if (!lhsTop && rhsTop) {
			return 1;
		}
		EMMessage lhsLastMsg = lhs.getLastMessage();
		EMMessage rhsLastMsg = rhs.getLastMessage();
		if (lhsLastMsg == null && rhsLastMsg == null) {
			return 0;
		}
		if (lhsLastMsg == null) {
			return 1;
		}
		if (rhsLastMsg == null) {
			return -1;
		}
		if (lhsLastMsg.getMsgTime() == rhsLastMsg.getMsgTime()) {
			return 0;
		}
		if (lhsLastMsg.getMsgTime() < rhsLastMsg.getMsgTime()) {
			return 1;
		}
		return -1;
	};

}