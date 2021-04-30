package com.hyphenate.easeui.widget.chatrow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.domain.EaseEmojicon;

/**
 * big emoji icons
 */
public class EaseChatRowBigExpression extends EaseChatRowText {

	private ImageView imageView;

	public static EaseChatRowBigExpression create(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType == EaseMessageAdapter.MESSAGE_TYPE_SENT_EXPRESSION ? R.layout.ease_row_sent_bigexpression :
				R.layout.ease_row_received_bigexpression, parent, false);
		return new EaseChatRowBigExpression(view);
	}

	public EaseChatRowBigExpression(View view) {
		super(view);
	}

	@Override
	protected void findView() {
		tvPercent =  itemView.findViewById(R.id.percentage);
		imageView =  itemView.findViewById(R.id.image);
	}

	@Override
	public void setUpView() {
		String emojiconId = message.getStringAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_EXPRESSION_ID, null);
		EaseEmojicon emojicon = null;
		if (EaseUI.getInstance().getEmojiconInfoProvider() != null) {
			emojicon = EaseUI.getInstance().getEmojiconInfoProvider().getEmojiconInfo(emojiconId);
		}
		if (emojicon != null) {
			if (emojicon.getBigIcon() != 0) {
				Glide.with(activity).load(emojicon.getBigIcon())
						.apply(new RequestOptions().placeholder(R.drawable.ease_default_expression))
						.into(imageView);
			}
			else if (emojicon.getBigIconPath() != null) {
				Glide.with(activity).load(emojicon.getBigIconPath())
						.apply(new RequestOptions().placeholder(R.drawable.ease_default_expression))
						.into(imageView);
			}
			else {
				Glide.with(activity).load(R.drawable.ease_default_expression).into(imageView);
			}
		}
		handleTextMessage();
	}
}
