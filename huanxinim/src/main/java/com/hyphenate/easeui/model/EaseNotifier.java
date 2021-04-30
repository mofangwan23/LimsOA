/************************************************************
 * * Hyphenate CONFIDENTIAL
 * __________________
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * NOTICE: All information contained herein is, and remains
 * the property of Hyphenate Inc.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Hyphenate Inc.
 */
package com.hyphenate.easeui.model;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.EmojiUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import java.util.List;
import java.util.Map;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EaseNotifier {

	private long mLastNotifyTime;
	private Context mContext;
	private Ringtone mRingtone;
	private Vibrator mVibrator;
	private AudioManager mAudioManager;
	private Handler mHandler = new Handler();

	public EaseNotifier(Context context) {
		this.mContext = context;
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * 创建通知
	 */
	public void executeNotify(Map<String, Integer> notifyMap, List<EMMessage> messages) {
		if (CoreZygote.getContext() == null || CoreZygote.getApplicationServices() == null) return;
		for (EMMessage message : messages) {
			executeNotify(notifyMap, message);
		}
	}

	@SuppressLint("WrongConstant")
	private void executeNotify(Map<String, Integer> notifyMap, EMMessage message) {
		String conversationId = message.conversationId();
		if (notifyMap.containsKey(conversationId)) {
			Integer integer = notifyMap.get(conversationId);
			notifyMap.put(conversationId, integer + 1);
		}
		else {
			notifyMap.put(conversationId, 1);
		}

		String name = getMessageFrom(message);
		Intent intent = new Intent("cn.flyrise.study.notification.NotificationReceiver.FROM_IM");

		int count = notifyMap.get(conversationId);
		intent.putExtra("notifyTitle", name);
		intent.putExtra("conversationId", conversationId);
		intent.putExtra("messageType", message.getChatType() == ChatType.GroupChat ? 2 : 1);
		String notifyText = "";

		if (message.getBody() instanceof EMTextMessageBody) {
			notifyText = ((EMTextMessageBody) message.getBody()).getMessage();
			notifyText = EmojiUtil.parseEmojiText(notifyText);
		}
		else if (message.getBody() instanceof EMImageMessageBody) {
			notifyText = "[图片]";
		}
		else if (message.getBody() instanceof EMNormalFileMessageBody) {
			notifyText = "[文件]";
		}
		else if (message.getBody() instanceof EMVoiceMessageBody) {
			notifyText = "[语音]";
		}
		else if (message.getBody() instanceof EMLocationMessageBody) {
			notifyText = "[位置]";
		}
		else if (message.getBody() instanceof EMVideoMessageBody) {
			notifyText = "[视频]";
		}
		else {
			notifyText = String.format("发来了%s条消息", name, count);
		}

		intent.putExtra("notifyText", notifyText);


		if(Build.VERSION.SDK_INT >= 26){
			ComponentName componentName=new ComponentName("cn.flyrise.study","cn.flyrise.feep.notification.NotificationReceiver");
			intent.setComponent(componentName);
			intent.addFlags(0x01000000);//解决在android8.0系统以上2个module之间发送广播接收不到的问题
		}

		mContext.sendBroadcast(intent);
	}

	private String getMessageFrom(EMMessage message) {
		String from;
		if (message.getChatType() == ChatType.GroupChat) {  // 群聊
			String conversationId = message.conversationId();
			EMGroup group = EMClient.getInstance().groupManager().getGroup(conversationId);
			from = group != null ? group.getGroupName() : CoreZygote.getConvSTServices().getCoversationName(conversationId);
		}
		else {                                              // 单聊
			AddressBook addressBook = CoreZygote.getAddressBookServices().queryUserInfo(message.getFrom());
			from = addressBook != null ? addressBook.name : "";
		}

		if (TextUtils.isEmpty(from)) {
			from = message.getFrom();
		}
		return from;
	}

	/**
	 * 播放消息声音
	 */
	public void executeMessageRingtone() {
		if (System.currentTimeMillis() - mLastNotifyTime < 1000) {
			return;
		}

		if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
			return;
		}

		if (mRingtone == null) {
			Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			mRingtone = RingtoneManager.getRingtone(mContext, notificationUri);
			if (mRingtone == null) {
				return;
			}
		}

		if (mRingtone.isPlaying()) {
			return;
		}

		mLastNotifyTime = System.currentTimeMillis();
		mRingtone.play();

		if (!TextUtils.isEmpty(Build.MANUFACTURER)
				&& Build.MANUFACTURER.toLowerCase().contains("samsung")) {  // 狗日的三星可能吃了迈炫，根本停不下来
			mHandler.postDelayed(() -> {
				if (mRingtone.isPlaying()) {
					mRingtone.stop();                                       // 强制停
				}
			}, 3000);
		}
	}

	/**
	 * 执行消息震动
	 */
	@SuppressLint("MissingPermission") public void executeMessageVibrate() {
		if (mVibrator == null) {
			mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		}

		long[] pattern = new long[]{0, 180, 80, 120};
		mVibrator.vibrate(pattern, -1);                                     // 震一次就好，震多伤身
	}

}
