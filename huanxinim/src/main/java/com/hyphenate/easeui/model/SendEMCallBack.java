package com.hyphenate.easeui.model;

import android.support.annotation.Keep;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseUiK.EmChatContent;

/**
 * Created by klc on 2017/7/12.
 */

@Keep
public class SendEMCallBack implements EMCallBack {

	private EMMessage message;
	private EMCallBack callBack;


	public SendEMCallBack(EMMessage message, EMCallBack callBack) {
		this.message = message;
		this.callBack = callBack;
	}

	@Override
	public void onSuccess() {
		message.setAttribute(EmChatContent.MESSAGE_ATTR_IS_REJECTION, false);
		if (callBack != null) {
			callBack.onSuccess();
		}
	}

	@Override
	public void onError(int errorCode, String s) {
		if (errorCode == 210) {
			message.setAttribute(EmChatContent.MESSAGE_ATTR_IS_REJECTION, true);
		}
		if (callBack != null) {
			callBack.onError(errorCode, s);
		}
	}

	@Override
	public void onProgress(int i, String s) {
		if (callBack != null) {
			callBack.onProgress(i, s);
		}
	}
}
