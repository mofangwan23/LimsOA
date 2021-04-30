package cn.flyrise.feep.push;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import com.google.gson.reflect.TypeToken;
import java.util.Map;

public class NiuBiNotification {

	private static final String KEY_SOUND = "open_notificaiton_sound";
	private static final String KEY_VIBRATE = "open_notification_vibrate";

	private static final String GROUP_KEY = "group_key";

	private static final String FE_MESSAGE = "feMessage";
	private static final String CHAT_MESSAGE = "chatMessage";
	public static final String UPDATE_MESSAGE = "updateMessage";

	private static long lastTime = 0;//最一次通知时间

	public static void notificationChannel(Context context) {//兼容8.0通知分类,设置之后更改很麻烦
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel(context, FE_MESSAGE, "企业消息", NotificationManager.IMPORTANCE_HIGH);

			createNotificationChannel(context, CHAT_MESSAGE, "聊天消息", NotificationManager.IMPORTANCE_HIGH);

			createNotificationChannel(context, UPDATE_MESSAGE, "版本更新", NotificationManager.IMPORTANCE_LOW);
		}
	}

	@TargetApi(Build.VERSION_CODES.O)
	private static void createNotificationChannel(Context context, String channelId, String channelName, int importance) {
		NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notificationManager != null) notificationManager.createNotificationChannel(channel);
	}

	//聊天通知
	public static Notification createSilenceNotification(Context context,
			String title, String content, String ticker,
			long when, PendingIntent contentIntent,
			@DrawableRes int smallIcon, @DrawableRes int largeIcon,
			@ColorRes int color, boolean autoCancel) {
		return buildNotification(context, title, content,
				ticker, when, contentIntent, smallIcon,
				largeIcon, color, autoCancel, true, CHAT_MESSAGE);
	}

	//OA消息通知
	public static Notification createNotification(Context context,
			String title, String content, String ticker,
			long when, PendingIntent contentIntent,
			@DrawableRes int smallIcon, @DrawableRes int largeIcon,
			@ColorRes int color, boolean autoCancel) {
		return buildNotification(context, title, content,
				ticker, when, contentIntent, smallIcon,
				largeIcon, color, autoCancel, false, FE_MESSAGE);
	}

	private static Notification buildNotification(Context context,
			String title, String content, String ticker,
			long when, PendingIntent contentIntent,
			@DrawableRes int smallIcon, @DrawableRes int largeIcon,
			@ColorRes int color, boolean autoCancel, boolean silence, String channellId) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channellId);
		builder.setContentTitle(title)
				.setContentText(content)
				.setContentIntent(contentIntent)
				.setSmallIcon(smallIcon)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), largeIcon))
				.setColor(context.getResources().getColor(color))
				.setAutoCancel(autoCancel)
				.setTicker(ticker)
				.setWhen(when)
				.setSound(null)
				.setVibrate(null)
				.setOngoing(false)
				.setGroup(GROUP_KEY)
				.setGroupSummary(false);

		if (silence) {
			return builder.build();
		}

		return appendSoundAndVibrate(builder);
	}

	//8.0以下有效，8.0以上所有消息提示都是通道设置死的，无法修改
	private static Notification appendSoundAndVibrate(NotificationCompat.Builder builder) {
		if (System.currentTimeMillis() - lastTime < 3 * 1000) {//如果连续通知时间小于3秒，不提示音乐和震动
			return builder.build();
		}
		lastTime = System.currentTimeMillis();

		Map<String, Boolean> status = getNotificationStatus();
		boolean isSound = status == null || (status.containsKey(KEY_SOUND) ? status.get(KEY_SOUND) : true);

		boolean isVibrate = status == null || (status.containsKey(KEY_VIBRATE) ? status.get(KEY_VIBRATE) : true);

		if (isSound && isVibrate) {
			builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
		}
		else if (isSound && !isVibrate) {
			builder.setDefaults(Notification.DEFAULT_SOUND);
		}
		else if (isVibrate && !isSound) {
			builder.setDefaults(Notification.DEFAULT_VIBRATE);
		}
		else {
			builder.setSound(null).setVibrate(null);
		}

		return builder.build();
	}

	private static Map<String, Boolean> getNotificationStatus() {
		String notificationStatus = SpUtil.get(PreferencesUtils.SETTING_NOTIFICATION_STATUS, "");
		Map<String, Boolean> status = null;
		if (!TextUtils.isEmpty(notificationStatus)) {
			status = GsonUtil.getInstance().fromJson(notificationStatus, new TypeToken<Map<String, Boolean>>() {
			}.getType());
		}
		return status;
	}
}
