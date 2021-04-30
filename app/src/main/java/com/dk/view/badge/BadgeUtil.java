package com.dk.view.badge;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.views.SplashActivity;
import com.hyphenate.chatui.utils.FeepPushManager;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.SpUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.leolin.shortcutbadger.ShortcutBadger;

public final class BadgeUtil {

	private static final int MIUI_NOTIFICATION_ID = 600;

	public static void setBadgeCount(Context context, int count) {
		int actualCount = count <= 0 ? 0 : Math.min(count, 99);
		if (!Build.MANUFACTURER.toLowerCase().contains("xiaomi")) {     // 非小米
			ShortcutBadger.applyCount(context, actualCount);
			return;
		}
		setBadgeOfMIUI(context, actualCount);
	}

	/**
	 * 设置MIUI的Badge
	 */
	private static void setBadgeOfMIUI(Context context, int count) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (count == 0) {
			nm.cancel(MIUI_NOTIFICATION_ID);
			return;
		}
		Intent intent;

		boolean isChatActivity = SpUtil.get("notification_acitivity", false);
		Class<? extends Activity> targetClass = isChatActivity ? FEMainActivity.class : SplashActivity.class;
		Context activity = CoreZygote.getApplicationServices().getFrontActivity();
		if (activity != null) {
			intent = new Intent(activity, targetClass);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra("notification_opent", true);
		}
		else {
			intent = new Intent(CoreZygote.getContext(), targetClass);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}

		PendingIntent pendingIntent = PendingIntent.getActivity(context,
				MIUI_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		String content = String.format(context.getResources().getString(R.string.notifiy_content), count);
		Notification notification = FeepPushManager.createSilenceNotification(context,
				context.getResources().getString(R.string.app_name),
				content, content, System.currentTimeMillis(), pendingIntent,
				R.drawable.icon,
				R.drawable.icon,
				R.color.app_icon_bg, true);
		if (notification == null) return;//去除推送后，会为空
		try {
			Field field = notification.getClass().getDeclaredField("extraNotification");
			Object extraNotification = field.get(notification);
			Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
			method.invoke(extraNotification, count);
		} catch (Exception e) {
			e.printStackTrace();
		}
		nm.cancel(MIUI_NOTIFICATION_ID);
		nm.notify(MIUI_NOTIFICATION_ID, notification);
	}
}
