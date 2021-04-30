package com.hyphenate.easeui.widget.chatrow;

import static com.hyphenate.easeui.adapter.EaseMessageAdapter.MESSAGE_TYPE_REC_VIDEO_CALL;
import static com.hyphenate.easeui.adapter.EaseMessageAdapter.MESSAGE_TYPE_REC_VOICE_CALL;
import static com.hyphenate.easeui.adapter.EaseMessageAdapter.MESSAGE_TYPE_SENT_VIDEO_CALL;
import static com.hyphenate.easeui.adapter.EaseMessageAdapter.MESSAGE_TYPE_SENT_VOICE_CALL;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.Direct;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.ui.VideoCallActivity;
import com.hyphenate.chatui.ui.VoiceCallActivity;
import com.hyphenate.easeui.EaseUiK;

;

public class ChatRowVoiceCall extends EaseChatRow {

	private TextView tvContent;

	public static ChatRowVoiceCall create(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		int layout = 0;
		switch (viewType) {
			case MESSAGE_TYPE_SENT_VOICE_CALL:
				layout = R.layout.ease_row_sent_voice_call;
				break;
			case MESSAGE_TYPE_REC_VOICE_CALL:
				layout = R.layout.ease_row_received_voice_call;
				break;
			case MESSAGE_TYPE_SENT_VIDEO_CALL:
				layout = R.layout.ease_row_sent_video_call;
				break;
			case MESSAGE_TYPE_REC_VIDEO_CALL:
				layout = R.layout.ease_row_received_video_call;
				break;
		}
		View view = inflater.inflate(layout, parent, false);
		return new ChatRowVoiceCall(view);
	}

	public ChatRowVoiceCall(View view) {
		super(view);
	}

	@Override
	protected void findView() {
		tvContent = itemView.findViewById(R.id.tv_chatcontent);
	}

	@Override
	protected void setUpView() {
		EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
		tvContent.setText(txtBody.getMessage());
	}

	@Override
	protected void onBubbleClick() {
		String userId = message.direct() == Direct.SEND ? message.getTo() : message.getFrom();
		if (message.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
			startVoiceCall(userId);
		} else if (message.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
			startVideoCall(userId);
		}
	}

	private void startVoiceCall(String toChatUsername) {
		if (!EMClient.getInstance().isConnected()) {
			Toast.makeText(activity, R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
		}
		else {
			activity.startActivity(new Intent(activity, VoiceCallActivity.class).putExtra("username", toChatUsername)
					.putExtra("isComingCall", false));
		}
	}

	private void startVideoCall(String toChatUsername) {
		if (!EMClient.getInstance().isConnected()) {
			Toast.makeText(activity, R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
		}
		else {
			activity.startActivity(new Intent(activity, VideoCallActivity.class).putExtra("username", toChatUsername)
					.putExtra("isComingCall", false));
		}
	}
}
