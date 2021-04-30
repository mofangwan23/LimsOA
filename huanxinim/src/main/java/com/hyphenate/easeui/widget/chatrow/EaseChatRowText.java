package com.hyphenate.easeui.widget.chatrow;

import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.exceptions.HyphenateException;

public class EaseChatRowText extends EaseChatRow {

	protected TextView contentView;

	public static EaseChatRowText create(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType == EaseMessageAdapter.MESSAGE_TYPE_REC_TXT ? R.layout.ease_row_received_message :
				R.layout.ease_row_sent_message, parent, false);
		return new EaseChatRowText(view);
	}

	public EaseChatRowText(View itemView) {
		super(itemView);
	}

	@Override
	protected void findView() {
		contentView = (TextView) itemView.findViewById(R.id.tv_chatcontent);
	}

	@Override
	public void setUpView() {
		EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
		Spannable span = EaseSmileUtils.getSmiledText(activity, txtBody.getMessage());
		contentView.setText(span, BufferType.SPANNABLE);
		handleTextMessage();
	}

	void handleTextMessage() {
		if (message.direct() == EMMessage.Direct.SEND) {
			setMsgCallBack();
			switch (message.status()) {
				case CREATE:
					progressBar.setVisibility(View.GONE);
					statusView.setVisibility(View.VISIBLE);
					break;
				case SUCCESS:
					progressBar.setVisibility(View.GONE);
					statusView.setVisibility(View.GONE);
					break;
				case FAIL:
					progressBar.setVisibility(View.GONE);
					statusView.setVisibility(View.VISIBLE);
					break;
				case INPROGRESS:
					progressBar.setVisibility(View.VISIBLE);
					statusView.setVisibility(View.GONE);
					break;
				default:
					break;
			}
		}
		else {
			if (!message.isAcked() && message.getChatType() == ChatType.Chat) {
				try {
					EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
				} catch (HyphenateException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onBubbleClick() {
	}

}
