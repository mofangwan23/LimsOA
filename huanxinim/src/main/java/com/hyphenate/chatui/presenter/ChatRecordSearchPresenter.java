package com.hyphenate.chatui.presenter;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by klc on 2017/3/17.
 */

public class ChatRecordSearchPresenter implements FEListContract.Presenter {

	private FEListContract.View<EMMessage> mView;
	private EMConversation conversation;
	private EMMessage lastMsg;
	private String mSearchKey;
	private boolean hasMore;

	public ChatRecordSearchPresenter(FEListContract.View<EMMessage> mView, String conversationId) {
		this.mView = mView;
		this.conversation = EMClient.getInstance().chatManager().getConversation(conversationId);
	}

	@Override
	public void onStart() {
	}

	@Override
	public void refreshListData() {
	}

	@Override
	public void refreshListData(String searchKey) {
		mSearchKey = searchKey;
		loadData(System.currentTimeMillis()).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(msgList -> {
					mView.refreshListData(msgList);
					hasMore = msgList.size() == 50;
				}, throwable -> mView.refreshListData(null));
	}

	@Override
	public void loadMoreData() {
		if (lastMsg == null) mView.loadMoreListFail();
		loadData(lastMsg.getMsgTime()).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(emMessages -> {
					mView.loadMoreListData(emMessages);
					hasMore = emMessages.size() == 50;
				}, throwable -> mView.loadMoreListFail());
	}


	private Observable<List<EMMessage>> loadData(long time) {
		return Observable.create(subscriber -> {
			List<EMMessage> resultList = conversation.searchMsgFromDB(mSearchKey, time, 100, null, EMConversation.EMSearchDirection.UP);
			List<EMMessage> filterMsgs = new ArrayList<>();
			for (EMMessage message : resultList) {
				EMTextMessageBody body = (EMTextMessageBody) message.getBody();
				String text = body.getMessage();
				String regex = "\\[\\(A:[1-7]?[0-9]\\)\\]";
				Pattern pat = Pattern.compile(regex);
				Matcher matcher = pat.matcher(text);
				text = matcher.replaceAll("");
				if (text.contains(mSearchKey))
					filterMsgs.add(message);
			}
			if (!CommonUtil.isEmptyList(filterMsgs)) {
				lastMsg = filterMsgs.get(filterMsgs.size() - 1);
			}
			Collections.reverse(filterMsgs);
			subscriber.onNext(filterMsgs);
			subscriber.onCompleted();
		});
	}

	@Override
	public boolean hasMoreData() {
		return hasMore;
	}

	public EMConversation getConversation() {
		return conversation;
	}
}
