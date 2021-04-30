package com.hyphenate.easeui.widget.chatrow;

import static com.hyphenate.easeui.EaseUiK.EmChatContent.MESSAGE_ATTR_NOT_SHOW_CONTENT;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatui.R;

public class EaseChatRowSystem extends EaseChatRow {

	private TextView contentView;

	public static EaseChatRowSystem create(ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.ease_row_system, parent, false);
		return new EaseChatRowSystem(view);
	}

	public EaseChatRowSystem(View view) {
		super(view);
	}

	@Override
	protected void findView() {
		contentView = (TextView) itemView.findViewById(R.id.tvRecall);
	}

	@Override
	protected void setUpView() {
		if (message.getBooleanAttribute(MESSAGE_ATTR_NOT_SHOW_CONTENT, false)) {
			contentView.setVisibility(View.GONE);
			mTvTime.setVisibility(View.GONE);
		}
		else {
			contentView.setVisibility(View.VISIBLE);
			EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
			contentView.setText(txtBody.getMessage());
		}
	}

	@Override
	protected void onBubbleClick() {

	}
}
