package cn.flyrise.feep.retrieval.repository;

import static cn.flyrise.feep.retrieval.bean.Retrieval.VIEW_TYPE_CONTENT;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_CHAT;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.retrieval.bean.ChatRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import com.hyphenate.chatui.retrieval.ChatMessage;
import com.hyphenate.chatui.retrieval.ChatMessagesRepository;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-09 16:27
 * 聊天记录检索
 */
public class ChatRetrievalRepository extends RetrievalRepository {

	private ChatMessagesRepository mChatRepository;

	@Override public void search(Subscriber<? super RetrievalResults> subscriber, String keyword) {
		if (!IMHuanXinHelper.getInstance().isImLogin()) {
			FELog.w("Hyphenate hasn't login.");
			subscriber.onNext(emptyResult());
			return;
		}

		if (mChatRepository == null) {
			mChatRepository = new ChatMessagesRepository();
		}

		this.mKeyword = keyword;
		this.mChatRepository.queryMessage(keyword, 3)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(chatMessages -> {
					List<ChatRetrieval> chatRetrievals = null;
					if (CommonUtil.nonEmptyList(chatMessages)) {

						chatRetrievals = new ArrayList<>();
						chatRetrievals.add((ChatRetrieval) header("聊天记录"));

						for (ChatMessage message : chatMessages) {
							chatRetrievals.add(createRetrievalChat(message, keyword));
						}

						if (chatMessages.size() >= 3) {
							chatRetrievals.add((ChatRetrieval) footer("更多聊天记录"));
						}
					}

					subscriber.onNext(new RetrievalResults.Builder()
							.retrievalType(getType())
							.retrievals(chatRetrievals)
							.create());
				}, exception -> {
					FELog.e("Chat message retrieval failed. Error: " + exception.getMessage());
					subscriber.onNext(emptyResult());
				});
	}

	private ChatRetrieval createRetrievalChat(ChatMessage chatMessage, String keyword) {
		ChatRetrieval chatRetrieval = new ChatRetrieval();
		chatRetrieval.viewType = VIEW_TYPE_CONTENT;
		chatRetrieval.retrievalType = TYPE_CHAT;

		chatRetrieval.content = fontDeepen(chatMessage.conversationName, keyword);
		chatRetrieval.extra = chatMessage.content;
		chatRetrieval.keyword = mKeyword;

		chatRetrieval.isGroup = chatMessage.isGroup;
		chatRetrieval.messageId = chatMessage.messageId;
		chatRetrieval.conversationId = chatMessage.conversationId;
		chatRetrieval.imageRes = chatMessage.imageRes;
		return chatRetrieval;
	}

	@Override protected int getType() {
		return TYPE_CHAT;
	}

	@Override protected Retrieval newRetrieval() {
		return new ChatRetrieval();
	}
}
