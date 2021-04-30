package com.hyphenate.easeui.controller;

import android.content.Context;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.model.EaseNotifier;

public final class EaseUI {

	private static EaseUI sInstance;
	private EaseSettingsProvider settingsProvider;
	private boolean isSDKInit = false;
	private EaseNotifier mNotifier = null;

	private EaseUI() {
	}

	public synchronized static EaseUI getInstance() {
		if (sInstance == null) {
			sInstance = new EaseUI();
		}
		return sInstance;
	}

	public synchronized boolean init(Context context, EMOptions options) {
		if (isSDKInit) {
			return true;
		}
		EMClient.getInstance().init(context, options);
		mNotifier = new EaseNotifier(context);
		isSDKInit = true;
		return true;
	}

	public EaseNotifier getNotifier() {
		return mNotifier;
	}

	public void setSettingsProvider(EaseSettingsProvider settingsProvider) {
		this.settingsProvider = settingsProvider;
	}

	public EaseSettingsProvider getSettingsProvider() {
		return settingsProvider;
	}

	public interface EaseEmojiconInfoProvider {

		EaseEmojicon getEmojiconInfo(String emojiconIdentityCode);
	}

	private EaseEmojiconInfoProvider emojiconInfoProvider;

	public EaseEmojiconInfoProvider getEmojiconInfoProvider() {
		return emojiconInfoProvider;
	}

	public void setEmojiconInfoProvider(EaseEmojiconInfoProvider emojiconInfoProvider) {
		this.emojiconInfoProvider = emojiconInfoProvider;
	}

	public interface EaseSettingsProvider {

		boolean isSpeakerOpened();
	}
}
