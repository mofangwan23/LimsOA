package com.hyphenate.easeui.widget.chatrow;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.DateUtil;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.model.Message;
import com.hyphenate.easeui.widget.EaseChatMessageList.MessageListItemClickListener;
import com.hyphenate.exceptions.HyphenateException;

/**
 * @author ZYP
 * @since 2017-03-29 12:00 扩展消息
 */
public class EaseChatRowExtend extends EaseChatRow {

	private TextView mTvAction;
	private TextView mTvTitle;
	private View mLayoutExtend;
	private String mModuleId;
	private TextView mTvExtendTime;

	private Message mFeMessage;

	public static EaseChatRowExtend create(ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.core_common_msg_card, parent, false);
		return new EaseChatRowExtend(view);
	}

	public EaseChatRowExtend(View view) {
		super(view);
	}

	@Override
	protected void findView() {
		mTvAction = itemView.findViewById(R.id.tv_action);
		mTvTitle = itemView.findViewById(R.id.tv_chattitle);
		mLayoutExtend = itemView.findViewById(R.id.layoutContentView);
		mTvExtendTime = itemView.findViewById(R.id.timestamp);
	}

	public void setView(Context context, EMMessage message, EMMessage preMessage, MessageListItemClickListener itemClickListener) {
		this.activity = (Activity) context;
		this.message = message;
		this.preMessage = preMessage;
		this.itemClickListener = itemClickListener;
		setUpBaseView();
		setClickListener();
	}

	@Override
	protected void setUpView() {
		mFeMessage = new Message();
		try {
			mModuleId = message.getStringAttribute("type");
			mFeMessage.setBusinessID(message.getStringAttribute("id"));
			mFeMessage.setMessageID(message.getStringAttribute("msgId"));
			mFeMessage.setUrl(message.getStringAttribute("url"));
			mFeMessage.setAction(message.getStringAttribute("action"));
			String title = message.getStringAttribute("title");
			String content = message.getStringAttribute("content");
			mFeMessage.setTitle(title);
			mFeMessage.setContent(TextUtils.equals(title, content) ? null : content);

		} catch (HyphenateException e) {
			e.printStackTrace();
		}
		String action = mFeMessage.getAction();
		String title = mFeMessage.getTitle();

		if (!TextUtils.isEmpty(action)) {
			mTvAction.setVisibility(View.VISIBLE);
			mTvAction.setText(action);
		}
		else {
			mTvAction.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(title)) {
			mTvTitle.setVisibility(View.VISIBLE);
			mTvTitle.setText(title);
		}
		else {
			mTvTitle.setVisibility(View.GONE);
		}

		String sendTime = DateUtil.formatTimeForDetail(message.getMsgTime());
		if (mTvExtendTime != null) {
			mTvExtendTime.setText(sendTime);
			mTvExtendTime.setVisibility(View.VISIBLE);
		}
		mFeMessage.setSendTime(sendTime);
	}

	@Override
	protected void onBubbleClick() {

	}


	@Override
	protected void setClickListener() {
		super.setClickListener();
		mLayoutExtend.setOnClickListener(view -> {
			if (itemClickListener != null) {
				itemClickListener.onExtendMessageClick(mModuleId, mFeMessage);
			}
		});
		mLayoutExtend.setOnLongClickListener(v -> {
			if (itemClickListener != null) {
				itemClickListener.onBubbleLongClick(message);
			}
			return true;
		});
	}


}
