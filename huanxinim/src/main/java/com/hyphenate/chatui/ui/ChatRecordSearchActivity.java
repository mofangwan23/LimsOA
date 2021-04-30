package com.hyphenate.chatui.ui;

import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.feep.core.base.component.FESearchListActivity;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.squirtlez.frouter.annotations.Route;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.adapter.ChatRecordSearchAdapter;
import com.hyphenate.chatui.presenter.ChatRecordSearchPresenter;
import com.hyphenate.easeui.busevent.ChatContent;
import org.greenrobot.eventbus.EventBus;

@Route("/im/chat/search")
public class ChatRecordSearchActivity extends FESearchListActivity<EMMessage> {

	private ChatRecordSearchAdapter mAdapter;
	private ChatRecordSearchPresenter mPresenter;
	private String mConversationId;

	@Override
	public void bindView() {
		super.bindView();
		this.listView.setCanRefresh(false);
	}

	@Override
	public void bindData() {
		et_Search.setHint(R.string.input_msg_key);
		mConversationId = getIntent().getStringExtra("conversationId");
		mAdapter = new ChatRecordSearchAdapter(this);
		mPresenter = new ChatRecordSearchPresenter(this, mConversationId);
		setAdapter(mAdapter);
		setPresenter(mPresenter);

		String keyword = getIntent().getStringExtra("keyword");
		if (!TextUtils.isEmpty(keyword)) {
			et_Search.setText(keyword);
			et_Search.setSelection(keyword.length());
			searchKey = keyword;
			myHandler.post(searchRunnable);
		}
		else {
			myHandler.postDelayed(() -> DevicesUtil.showKeyboard(et_Search), 500);
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mAdapter.setOnItemClickListener((view, object) -> {
			EMMessage emMessage = (EMMessage) object;
			emMessage.getMsgId();
			EventBus.getDefault().post(new ChatContent.SeekToMsgEvent(emMessage.getMsgId()));

			Intent intent = new Intent(ChatRecordSearchActivity.this, ChatActivity.class);
			intent.putExtra("Extra_chatID", mConversationId);
			EMConversation conversation = mPresenter.getConversation();
			if (conversation.isGroup()) {
				intent.putExtra("Extra_chatType", 0X104);
			}

			startActivity(intent);
		});
	}
}
