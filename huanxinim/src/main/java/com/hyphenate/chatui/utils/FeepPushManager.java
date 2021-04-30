package com.hyphenate.chatui.utils;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import cn.flyrise.feep.push.NiuBiNotification;
import cn.flyrise.feep.push.PushTargetManager;

/**
 * 新建：陈冕;
 * 日期： 2018-8-28-15:24.
 * 推送模块管理
 */
public class FeepPushManager {

	public static void init(Application context) {
		PushTargetManager.getInstance().init(context);//推送初始化
	}

	public static void setNotificationOpen(Boolean isStartNotification) {//推送初始化开关推送
		PushTargetManager.getInstance().setNotificationOpen(isStartNotification);
	}

	public static void deleteAlias() {
		PushTargetManager.getInstance().deleteAlias();
	}

	public static void reInitAllalias() {
		PushTargetManager.getInstance().reInitAllalias();
	}

	public static String getPushAppId() {//注册推送的appid
		return PushTargetManager.getInstance().getAppId();
	}

	public static String getPushKey() {//获取当前注册的appkey
		return PushTargetManager.getInstance().getAppkey();
	}

	public static String getPushToken() {//推送唯一值
		return PushTargetManager.getInstance().getCurrentToken();
	}


	//聊天通知
	public static Notification createSilenceNotification(Context context,
			String title, String content, String ticker,
			long when, PendingIntent contentIntent,
			@DrawableRes int smallIcon, @DrawableRes int largeIcon,
			@ColorRes int color, boolean autoCancel) {
//		return null;
		return NiuBiNotification.createSilenceNotification(context, title, content,
				ticker, when, contentIntent, smallIcon,
				largeIcon, color, autoCancel);
	}

	//OA消息通知
	public static Notification createNotification(Context context,
			String title, String content, String ticker,
			long when, PendingIntent contentIntent,
			@DrawableRes int smallIcon, @DrawableRes int largeIcon,
			@ColorRes int color, boolean autoCancel) {
//		return null;
		return NiuBiNotification.createNotification(context, title, content,
				ticker, when, contentIntent, smallIcon,
				largeIcon, color, autoCancel);
	}
}
