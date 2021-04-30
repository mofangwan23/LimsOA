package com.hyphenate.easeui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.utils.EaseSmileUtils;

/**
 * primary menu
 */
public class EaseChatPrimaryMenu extends EaseChatPrimaryMenuBase implements OnClickListener {

	private EaseChatEditText editText;
	private View buttonSetModeKeyboard;
	private LinearLayout edittext_layout;
	private View buttonSetModeVoice;
	private View buttonSend;
	private View buttonPressToSpeak;
	public ImageView faceChecked;
	private Button buttonMore;
	public Button rlButtonSpeak;

	private TextView mReplyTextView;

	public EaseChatPrimaryMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public EaseChatPrimaryMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EaseChatPrimaryMenu(Context context) {
		super(context);
		init(context, null);
	}

	private void init(final Context context, AttributeSet attrs) {
		LayoutInflater.from(context).inflate(R.layout.ease_widget_chat_primary_menu, this);
		editText = (EaseChatEditText) findViewById(R.id.et_sendmessage);
		buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
		edittext_layout = (LinearLayout) findViewById(R.id.edittext_layout);
		buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
		buttonSend = findViewById(R.id.btn_send);
		buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
		faceChecked = (ImageView) findViewById(R.id.iv_face_checked);
		RelativeLayout faceLayout = (RelativeLayout) findViewById(R.id.rl_face);
		buttonMore = (Button) findViewById(R.id.btn_more);
		rlButtonSpeak = (Button) findViewById(R.id.rl_face_btn_set_mode_keyboard);
		edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_normal);
		mReplyTextView = (TextView) findViewById(R.id.tv_replyLabel);

		buttonSend.setOnClickListener(this);
		buttonSetModeKeyboard.setOnClickListener(this);
		buttonSetModeVoice.setOnClickListener(this);
		buttonMore.setOnClickListener(this);
		faceLayout.setOnClickListener(this);
		editText.setOnClickListener(this);
		rlButtonSpeak.setOnClickListener(this);
		faceChecked.setOnClickListener(this);
		editText.requestFocus();

		editText.setOnFocusChangeListener((v, hasFocus) -> {
			setEditTextBottomLine(hasFocus);
		});
		// listen the text change
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!TextUtils.isEmpty(s)) {
					buttonMore.setVisibility(View.GONE);
					buttonSend.setVisibility(View.VISIBLE);
				}
				else {
					buttonMore.setVisibility(View.VISIBLE);
					buttonSend.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		editText.setCursorVisible(false);
		editText.setOnMenuItemClick(() -> {
			String text = editText.getText().toString();
			Spannable spannable = EaseSmileUtils.getSmallSmiledText(getContext(), text);
			editText.setText(spannable, TextView.BufferType.SPANNABLE);
			editText.setSelection(text.length());
		});
		editText.setOnKeyListener((v, keyCode, event) -> {
			if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
				if (editText.getText().length() == 0 && !TextUtils.isEmpty(getReplyMessage())) {
					hideReplyTextView();
				}
			}
			return false;
		});
		buttonPressToSpeak.setOnTouchListener((v, event) -> listener != null && listener.onPressToSpeakBtnTouch(v, event));
	}

	public void setEditTextBottomLine(boolean isHasFocus) {
		editText.setCursorVisible(isHasFocus);
		if (isHasFocus) {
			edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_active);
		}
		else {
			edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_normal);
		}
	}

	/**
	 * append emoji icon to editText
	 */
	public void onEmojiconInputEvent(CharSequence emojiContent) {
		editText.append(emojiContent);
	}

	/**
	 * delete emojicon
	 */
	public void onEmojiconDeleteEvent() {
		if (!TextUtils.isEmpty(editText.getText())) {
			KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
			editText.dispatchKeyEvent(event);
		}
	}

	/**
	 * on clicked event
	 */
	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.btn_send) {
			if (listener != null) {
				String s = editText.getText().toString();
				editText.setText("");
				listener.onSendBtnClicked(s);
			}
		}
		else if (id == R.id.btn_set_mode_voice) {
			setModeVoice();
			if (listener != null) {
				listener.onToggleVoiceBtnClicked();
			}
			if (rlButtonSpeak.getVisibility() == VISIBLE) {
				faceChecked.setVisibility(VISIBLE);
				rlButtonSpeak.setVisibility(GONE);
			}
		}
		else if (id == R.id.btn_set_mode_keyboard) {
			setModeKeyboard();
			DevicesUtil.showKeyboard(editText);
		}
		else if (id == R.id.et_sendmessage) {
			edittext_layout.setBackgroundResource(R.drawable.ease_input_bar_bg_active);
			editText.setCursorVisible(true);
			if (listener != null) {
				listener.onEditTextClicked();
			}
			if (rlButtonSpeak.getVisibility() == VISIBLE) {
				faceChecked.setVisibility(VISIBLE);
				rlButtonSpeak.setVisibility(GONE);
			}
		}
		else if (id == R.id.rl_face_btn_set_mode_keyboard) {    // 右边键盘按钮
			setModeKeyboard();
			DevicesUtil.showKeyboard(editText);
			setEditTextBottomLine(true);
			if (listener != null) {
				listener.onKeyboardBtnClick();
			}
			if (rlButtonSpeak.getVisibility() == VISIBLE) {
				faceChecked.setVisibility(VISIBLE);
				rlButtonSpeak.setVisibility(GONE);
			}
		}
		else if (id == R.id.iv_face_checked) {                  // 右边表情按钮
			if (faceChecked.getVisibility() == VISIBLE) {
				faceChecked.setVisibility(GONE);
				rlButtonSpeak.setVisibility(VISIBLE);
			}
			if (listener != null) {
				listener.onToggleEmojiconClicked();
			}
		}
		else if (id == R.id.btn_more) {                         // 最右边扩展按钮
			buttonSetModeVoice.setVisibility(View.VISIBLE);
			buttonSetModeKeyboard.setVisibility(View.GONE);
			edittext_layout.setVisibility(View.VISIBLE);
			buttonPressToSpeak.setVisibility(View.GONE);
			if (listener != null) {
				listener.onToggleExtendClicked();
			}
		}
	}

	/**
	 * show voice icon when speak bar is touched
	 */
	protected void setModeVoice() {
		hideKeyboard();
		edittext_layout.setVisibility(View.GONE);
		buttonSetModeVoice.setVisibility(View.GONE);
		buttonSetModeKeyboard.setVisibility(View.VISIBLE);
		buttonSend.setVisibility(View.GONE);
		buttonMore.setVisibility(View.VISIBLE);
		buttonPressToSpeak.setVisibility(View.VISIBLE);

	}

	/**
	 * show keyboard
	 */
	protected void setModeKeyboard() {
		edittext_layout.setVisibility(View.VISIBLE);
		buttonSetModeKeyboard.setVisibility(View.GONE);
		buttonSetModeVoice.setVisibility(View.VISIBLE);
		editText.requestFocus();
		buttonPressToSpeak.setVisibility(View.GONE);
		if (TextUtils.isEmpty(editText.getText())) {
			buttonMore.setVisibility(View.VISIBLE);
			buttonSend.setVisibility(View.GONE);
		}
		else {
			buttonMore.setVisibility(View.GONE);
			buttonSend.setVisibility(View.VISIBLE);
		}

	}


	@Override
	public void onExtendMenuContainerHide() {
	}

	@Override
	public void onTextInsert(CharSequence text) {
		int start = editText.getSelectionStart();
		Editable editable = editText.getEditableText();
		editable.insert(start, text);
		setModeKeyboard();
	}

	@Override
	public EditText getEditText() {
		return editText;
	}

	@Override
	public View getEmotionButton() {
		return faceChecked;
	}

	@Override
	public void setReplyMessage(String replyMessage) {
		mReplyTextView.setVisibility(View.VISIBLE);
		mReplyTextView.setText(replyMessage);
	}

	@Override
	public String getReplyMessage() {
		return mReplyTextView.getText().toString();
	}

	@Override
	public TextView getReplyTextView() {
		return this.mReplyTextView;
	}

	@Override
	public void hideReplyTextView() {
		mReplyTextView.setVisibility(View.GONE);
		mReplyTextView.setText("");
	}

}
