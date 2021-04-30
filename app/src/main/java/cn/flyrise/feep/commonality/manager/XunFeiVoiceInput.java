package cn.flyrise.feep.commonality.manager;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import cn.flyrise.feep.core.common.VoiceRecognizer.MscRecognizerListener;
import cn.flyrise.feep.recod.VoiceInputView;

/**
 * 新建：陈冕;
 * 日期： 2018-8-28-17:08.
 * 讯飞语音输入管理
 */
public class XunFeiVoiceInput {

	private VoiceInputView voiceInputView;

	public XunFeiVoiceInput(Context context) {
		voiceInputView = new VoiceInputView(context);
	}

	//设置语音识别对话框监听器
	public void setOnRecognizerDialogListener(MscRecognizerListener recognizerListeners) {
		voiceInputView.setOnRecognizerDialogListener(recognizerListeners);
	}

	public void show() {
		voiceInputView.showIatDialog();
	}

	public void dismiss() {
		voiceInputView.dismiss();
	}

	public static void setVoiceInputText(EditText view, String text, int selection) {
		if (view == null) return;
		String mText = view.getText().toString().trim();
		String start = mText.substring(0, selection);
		String mEnd = mText.substring(selection, mText.length());
		String inputText;
		if (!TextUtils.isEmpty(mText)) {
			StringBuilder stringBuffer = new StringBuilder();
			inputText = stringBuffer.append(start).append(text).append(mEnd).toString();
		}
		else {
			inputText = text;
		}
		view.setText(inputText);
		try {
			view.setSelection(selection + text.length());
		} catch (Exception ex) {
			int length = view.getText().toString().length();
			view.setSelection(length);
		}
	}

}
