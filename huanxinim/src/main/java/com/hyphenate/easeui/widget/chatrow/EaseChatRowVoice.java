package com.hyphenate.easeui.widget.chatrow;

import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.util.EMLog;

public class EaseChatRowVoice extends EaseChatRowFile {

	private ImageView voiceImageView;
	private TextView voiceLengthView;
	private ImageView readStatusView;


	public static EaseChatRowVoice create(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType == EaseMessageAdapter.MESSAGE_TYPE_SENT_VOICE ? R.layout.ease_row_sent_voice :
				R.layout.ease_row_received_voice, parent, false);
		return new EaseChatRowVoice(view);
	}

	private EaseChatRowVoice(View view) {
		super(view);
	}

	@Override
	protected void findView() {
		voiceImageView = (ImageView) itemView.findViewById(R.id.iv_voice);
		voiceLengthView = (TextView) itemView.findViewById(R.id.tv_length);
		readStatusView = (ImageView) itemView.findViewById(R.id.iv_unread_voice);
	}

	@Override
	protected void setUpView() {
		EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
		int len = voiceBody.getLength();
		if (len > 0) {
			voiceLengthView.setText(voiceBody.getLength() + "\"");
			voiceLengthView.setVisibility(View.VISIBLE);
		}
		else {
			voiceLengthView.setVisibility(View.INVISIBLE);
		}
		if (EaseChatRowVoicePlayClickListener.playMsgId != null
				&& EaseChatRowVoicePlayClickListener.playMsgId.equals(message.getMsgId()) && EaseChatRowVoicePlayClickListener.isPlaying) {
			AnimationDrawable voiceAnimation;
			if (message.direct() == EMMessage.Direct.RECEIVE) {
				voiceImageView.setImageResource(R.drawable.voice_from_icon);
			}
			else {
				voiceImageView.setImageResource(R.drawable.voice_to_icon);
			}
			voiceAnimation = (AnimationDrawable) voiceImageView.getDrawable();
			voiceAnimation.start();
		}
		else {
			if (message.direct() == EMMessage.Direct.RECEIVE) {
				voiceImageView.setImageResource(R.drawable.ease_chatfrom_voice_playing);
			}
			else {
				voiceImageView.setImageResource(R.drawable.ease_chatto_voice_playing);
			}
		}

		if (message.direct() == EMMessage.Direct.RECEIVE) {
			if (message.isListened()) {
				// hide the unread icon
				readStatusView.setVisibility(View.INVISIBLE);
			}
			else {
				readStatusView.setVisibility(View.VISIBLE);
			}
			EMLog.d(TAG, "it is receive msg");
			if (voiceBody.downloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
					voiceBody.downloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING) {
				progressBar.setVisibility(View.VISIBLE);
				setMsgCallBack();
			}
			else {
				progressBar.setVisibility(View.INVISIBLE);

			}
			return;
		}
		// until here, handle sending voice message
		handleSendMessage();
	}


	@Override
	protected void onBubbleClick() {
		new EaseChatRowVoicePlayClickListener(message, voiceImageView, readStatusView, this, activity).onClick(bubbleLayout);
	}

}
