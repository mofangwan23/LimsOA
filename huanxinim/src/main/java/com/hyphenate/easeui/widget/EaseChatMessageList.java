package com.hyphenate.easeui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import cn.flyrise.feep.core.common.FELog;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.model.ChatMessageProvider;
import com.hyphenate.easeui.model.Message;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowVoicePlayClickListener;
import java.util.List;

public class EaseChatMessageList extends RelativeLayout {

	protected static final String TAG = "EaseChatMessageList";
	protected RecyclerView listView;
	protected Context context;
	protected EMConversation mConversation;
	protected EaseMessageAdapter mAdapter;
	protected ChatMessageProvider mProvide;

	private LinearLayoutManager layoutManager;

	private final int REFRESH_MESSAGE = 200;
	private final int REFRESH_MESSAGE_DEALY = 200;
	private boolean isActivityPause = false;//界面已停止,防止键盘弹出异常滚动

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			mConversation.markAllMessagesAsRead();
			int position = msg.arg1;
			if (position != -1) {
				notifyDataSetChanged(mProvide.getMemoryMsg());
				layoutManager.scrollToPositionWithOffset(position, 0);
			}
			else {
				boolean toLast = layoutManager.findLastVisibleItemPosition() == mAdapter.getItemCount() - 1;
				notifyDataSetChanged(mProvide.getMemoryMsg());
				if (toLast) {
					layoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
				}
			}
		}
	};

	public EaseChatMessageList(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs);
	}

	public EaseChatMessageList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public EaseChatMessageList(Context context) {
		super(context);
		initView(context);
	}

	public void setActivityPause(boolean isActivityPause) {
		this.isActivityPause = isActivityPause;
	}

	private void initView(Context context) {
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.ease_chat_message_list, this);
		listView = findViewById(R.id.list);
		layoutManager = new LinearLayoutManager(context);
		listView.setLayoutManager(layoutManager);
		listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (newState == RecyclerView.SCROLL_STATE_IDLE && mAdapter.hasHeaderView()
						&& layoutManager.findFirstVisibleItemPosition() == 0) {
					loadMoreMsg();
				}
			}
		});
		listView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
			if (bottom < oldBottom && !isActivityPause) {
				refreshSelectLast();
			}
		});
	}

	/**
	 * init widget
	 */
	public void initData(EMConversation conversation, ChatMessageProvider provider) {
		mConversation = conversation;
		mAdapter = new EaseMessageAdapter(context);
		mAdapter.setHasStableIds(true);
		listView.setAdapter(mAdapter);
		mAdapter.setImageLoadListener(this::refresh);
		mProvide = provider;
		mProvide.getInitMsg();
		refreshSelectLast();
	}

	/**
	 * refresh
	 */
	public void refresh() {
		android.os.Message message = new android.os.Message();
		message.arg1 = -1;
		mHandler.removeMessages(REFRESH_MESSAGE);
		mHandler.sendMessageDelayed(message, REFRESH_MESSAGE_DEALY);
	}

	/**
	 * refresh and jump to the last
	 */
	public void refreshSelectLast() {
		android.os.Message message = new android.os.Message();
		message.arg1 = mProvide.getMemoryMsg().size() - 1;
		mHandler.removeMessages(REFRESH_MESSAGE);
		mHandler.sendMessageDelayed(message, REFRESH_MESSAGE_DEALY);
		FELog.e("loadMsg Count:" + mConversation.getAllMsgCount());
	}

	public void seekToMsg(String msgID) {
		List<EMMessage> messageList = mProvide.getMagToTarget(msgID);
		android.os.Message message = new android.os.Message();
		message.arg1 = mProvide.getMsgPosition(messageList, msgID);
		mHandler.removeMessages(REFRESH_MESSAGE);
		mHandler.sendMessageDelayed(message, REFRESH_MESSAGE_DEALY);
	}

	public void loadMoreMsg() {
		List<EMMessage> messageList = mProvide.loadMoreMsg();
		android.os.Message message = new android.os.Message();
		message.arg1 = messageList.size() - 1;
		mHandler.removeMessages(REFRESH_MESSAGE);
		mHandler.sendMessageDelayed(message, REFRESH_MESSAGE_DEALY);
	}

	private void notifyDataSetChanged(List<EMMessage> messageList) {
		if (mProvide.isHasMore()) {
			mAdapter.setHeaderView(R.layout.ease_chat_message_list_head);
		}
		else {
			mAdapter.removeHeaderView();
		}
		mAdapter.refresh(messageList);
	}

	public RecyclerView getListView() {
		return listView;
	}

	public interface MessageListItemClickListener {

		void onResendClick(EMMessage message);

		void onBubbleLongClick(EMMessage message);

		void onUserAvatarClick(String username);

		void onUserAvatarLongClick(String username);

		void onExtendMessageClick(String moduleId, Message message);

	}

	/**
	 * set click listener
	 */
	public void setItemClickListener(MessageListItemClickListener listener) {
		if (mAdapter != null) {
			mAdapter.setItemClickListener(listener);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (EaseChatRowVoicePlayClickListener.currentPlayListener != null && EaseChatRowVoicePlayClickListener.isPlaying) {
			EaseChatRowVoicePlayClickListener.currentPlayListener.release();
		}
	}
}
