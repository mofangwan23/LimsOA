package com.hyphenate.easeui.widget.emojicon;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.hyphenate.easeui.domain.EaseEmojicon;

public class EaseEmojiconMenuBase extends LinearLayout {

	protected EaseEmojiconMenuListener listener;

	public EaseEmojiconMenuBase(Context context) {
		super(context);
	}

	public EaseEmojiconMenuBase(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EaseEmojiconMenuBase(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	/**
	 * set emojicon menu listener
	 */
	public void setEmojiconMenuListener(EaseEmojiconMenuListener listener) {
		this.listener = listener;
	}

	public interface EaseEmojiconMenuListener {

		/**
		 * on emojicon clicked
		 */
		void onExpressionClicked(EaseEmojicon emojicon);

		/**
		 * on delete image clicked
		 */
		void onDeleteImageClicked();
	}
}
