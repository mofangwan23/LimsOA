package cn.flyrise.feep.particular.views;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.BadgeView;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.LanguageManager;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;


/**
 * @author ZYP
 * @since 2016-10-27 10:07
 */
public class ParticularReplyEditView extends LinearLayout {

	private View mAttachmentBtn;
	private TextView mTvReplySubmit;
	private TextView mTvTextSize;
	private EditText mEtReplyContent;
	private BadgeView mTvAttachmentSize;
	private OnClickListener mRecordButtonClickListener;

	private OnKeyListener mKeyListener;

	private int maxTextNum = -1;

	private Context mContext;

	public ParticularReplyEditView(Context context) {
		this(context, null);
	}

	public ParticularReplyEditView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ParticularReplyEditView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_particular_bottom_reply, this);
		this.mContext = context;
		mAttachmentBtn = findViewById(R.id.layoutAttachmentBtn);
		mTvAttachmentSize = findViewById(R.id.tvAttachmentSize);
		mEtReplyContent = findViewById(R.id.etReplyContent);
		mTvReplySubmit = findViewById(R.id.tvReplySubmit);
		mTvTextSize = findViewById(R.id.text_size);
		if (LanguageManager.getCurrentLanguage() == LanguageManager.LANGUAGE_TYPE_CN) {
			findViewById(R.id.btnVoiceInput).setVisibility(View.VISIBLE);
			findViewById(R.id.btnVoiceInput).setOnClickListener(mRecordButtonClickListener);
		}
		setFocusable();
		mEtReplyContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Editable editable = mEtReplyContent.getText();
				if (maxTextNum != -1) {
					int len = mEtReplyContent.length();
					if (len > maxTextNum) {
						int selEndIndex = Selection.getSelectionEnd(editable);
						String str = editable.toString();
						//截取新字符串
						String newStr = str.substring(0, maxTextNum);
						mEtReplyContent.setText(newStr);
						editable = mEtReplyContent.getText();
						//新字符串的长度
						int newLen = editable.length();
						//旧光标位置超过字符串长度
						if (selEndIndex > newLen) {
							selEndIndex = editable.length();
						}
						//设置新光标所在的位置
						Selection.setSelection(editable, selEndIndex);
						String text = String.format(mContext.getResources().getString(R.string.metting_text_nums), maxTextNum);
						if (!TextUtils.isEmpty(text)) {
							mTvTextSize.setVisibility(View.VISIBLE);
							mTvTextSize.setText(text);
						}
					}
					else {
						mTvTextSize.setVisibility(GONE);
					}
				}
				SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, editable.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		String reply = SpUtil.get(PreferencesUtils.SAVE_REPLY_DATA, "");
		if (!TextUtils.isEmpty(reply)) {
			mEtReplyContent.setText(reply);
			mEtReplyContent.setSelection(reply.length());
		}
	}

	public void setFocusable() {
		if (mEtReplyContent == null) return;
		mEtReplyContent.requestFocus();
//		mEtReplyContent.setFocusable(true);
//		mEtReplyContent.setFocusableInTouchMode(true);
	}

	public void setWithAttachment(boolean withAttachment) {
		if (mAttachmentBtn != null) {
			mAttachmentBtn.setVisibility(withAttachment ? View.VISIBLE : View.GONE);
		}
	}

	public void setSubmitButtonText(String submitButtonText) {
		if (mTvReplySubmit != null) {
			mTvReplySubmit.setText(submitButtonText);
		}
	}

	public void setEditTextContent(String content) {
		if (mEtReplyContent != null) {
			mEtReplyContent.setText(content);
			mEtReplyContent.setSelection(content.length());
		}
	}

	public void setMaxTextNum(int maxTextNum) {
		this.maxTextNum = maxTextNum;
	}

	public void setAttachmentButtonVisibility(int visibility) {
		mAttachmentBtn.setVisibility(visibility);
	}

	public void setOnRecordButtonClickListener(OnClickListener listener) {
		this.mRecordButtonClickListener = listener;
		if (findViewById(R.id.btnVoiceInput).getVisibility() == View.VISIBLE) {
			findViewById(R.id.btnVoiceInput).setOnClickListener(mRecordButtonClickListener);
		}
	}

	public void setOnAttachmentButtonClickListener(OnClickListener listener) {
		if (mAttachmentBtn != null) {
			mAttachmentBtn.setOnClickListener(listener);
		}
	}

	public void setOnReplySubmitClickListener(OnClickListener listener) {
		if (mTvReplySubmit != null) {
			this.mTvReplySubmit.setOnClickListener(listener);
		}
	}

	public void setAttachmentSize(int size) {
		if (size > 0) {
			mTvAttachmentSize.setVisibility(View.VISIBLE);
			mTvAttachmentSize.setText(size + "");
			return;
		}
		mTvAttachmentSize.setVisibility(GONE);
	}

	public EditText getReplyEditText() {
		return this.mEtReplyContent;
	}

	public String getReplyContent() {
		String replyContent = mEtReplyContent.getText().toString();
		return replyContent.trim();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		FELog.i("dispatchKeyEvent : " + event.getAction());
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				|| event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
			if (mKeyListener != null) {
				mKeyListener.onKey(this, KeyEvent.KEYCODE_BACK, event);
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void setOnKeyListener(OnKeyListener keyListener) {
		mKeyListener = keyListener;
		super.setOnKeyListener(keyListener);
	}


	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				|| event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
			if (mKeyListener != null) {
				mKeyListener.onKey(this, KeyEvent.KEYCODE_BACK, event);
				return true;
			}
		}
		return super.dispatchKeyEventPreIme(event);
	}

}
