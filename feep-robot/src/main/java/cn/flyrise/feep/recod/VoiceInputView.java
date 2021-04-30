/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-10-10 下午2:44:20
 */

package cn.flyrise.feep.recod;

import android.content.Context;
import cn.flyrise.feep.core.common.VoiceRecognizer.MscRecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 类功能描述：语音输入</br>
 * @author 钟永健
 * @version 1.0</ br> 修改时间：2012-10-10</br> 修改备注：</br>
 * 修改2016-7-5
 */
public class VoiceInputView {

	private RecognizerDialog iatDialog;
	private MscRecognizerListener recognizerListener;
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<>();

	public VoiceInputView(Context context) {
		iatDialog = new RecognizerDialog(context, null);
		iatDialog.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置返回结果格式
		iatDialog.setParameter(SpeechConstant.RESULT_TYPE, "json");
		// 设置语言
		iatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		iatDialog.setParameter(SpeechConstant.VAD_BOS, "4000");
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		iatDialog.setParameter(SpeechConstant.VAD_EOS, "1000");
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		iatDialog.setParameter(SpeechConstant.ASR_PTT, "0");
		iatDialog.setListener(new RecognizerDialogListener() {
			@Override
			public void onResult(com.iflytek.cloud.RecognizerResult recognizerResult, boolean b) {
				if (!b) return;
				String text = printResult(recognizerResult);
				if (recognizerListener != null) {
					recognizerListener.onResult(text);
				}
			}

			@Override
			public void onError(SpeechError speechError) {
			}
		});
	}

	private String printResult(RecognizerResult results) {
		String data = results.getResultString();
		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(data);
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, parseIatResult(data));

		StringBuilder resultBuffer = new StringBuilder();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}

		return resultBuffer.toString();
	}

	public void showIatDialog() {
		if (iatDialog != null) iatDialog.show();
	}

	//设置语音识别对话框监听器
	public void setOnRecognizerDialogListener(MscRecognizerListener recognizerListener) {
		this.recognizerListener = recognizerListener;
	}

	public void dismiss() {
		if (iatDialog != null && iatDialog.isShowing()) {
			iatDialog.dismiss();
		}
	}

	public void destory() {
		if (iatDialog != null && iatDialog.isShowing()) {
			iatDialog.dismiss();
		}
		assert iatDialog != null;
		iatDialog.destroy();
		recognizerListener = null;
	}

	private String parseIatResult(String json) {
		StringBuilder ret = new StringBuilder();
		try {
			JSONTokener tokener = new JSONTokener(json);
			JSONObject joResult = new JSONObject(tokener);
			JSONArray words = joResult.getJSONArray("ws");
			for (int i = 0; i < words.length(); i++) {// 转写结果词，默认使用第一个结果
				JSONArray items = words.getJSONObject(i).getJSONArray("cw");
				JSONObject obj = items.getJSONObject(0);
				ret.append(obj.getString("w"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret.toString();
	}
}
