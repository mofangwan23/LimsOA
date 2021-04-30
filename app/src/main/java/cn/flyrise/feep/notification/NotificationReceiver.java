package cn.flyrise.feep.notification;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.views.SplashActivity;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.notification.NotificationMessage;
import cn.flyrise.feep.core.notification.ReceiverInfo;
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils;
import cn.flyrise.feep.event.EventJPushRefreshMessage;
import cn.flyrise.feep.push.EventHuaweiPushInitSuccess;
import com.dk.view.badge.BadgeUtil;
import com.hyphenate.chatui.ui.ChatActivity;
import com.hyphenate.chatui.utils.FeepPushManager;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.greenrobot.eventbus.EventBus;

/**
 * 接收所有消息推送的消息
 */
public class NotificationReceiver extends BroadcastReceiver {

	private static final String ACTION_PUSH_CLICK = "cn.flyrise.study.notification.NotificationReceiver.ACTION_PUSH_CLICK";
	private static final String ACTION_PUSH_MESSAGE = "cn.flyrise.study.notification.NotificationReceiver.PUSH_MESSAGE";
	private static final String ACTION_PUSH_NOTIFICATION = "cn.flyrise.study.notification.NotificationReceiver.PUSH_NOTIFICATION";
	private static final String ACTION_FROM_IM = "cn.flyrise.study.notification.NotificationReceiver.FROM_IM";
	private static final String ACTION_PUSH_TOEN = "cn.flyrise.study.notification.NotificationReceiver.PUSH_TOKEN";

	private static final String NOTIFY_TITLE = CommonUtil.getString(R.string.app_name);
	private static int sNotificationId = 0;                             // 消息最多显示五条
	private static Map<String, Integer> sNotifyIdMap = new HashMap<>(); // 存储消息对应的 NotifyId

	private static final int NOTIFICATION_NUMS = 3;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		ReceiverInfo receiverInfo = getReceiverInfo(intent);
		if (TextUtils.equals(ACTION_PUSH_MESSAGE, action) && receiverInfo != null) {
			FELog.i("push-handle:push_message:" + GsonUtil.getInstance().toJson(receiverInfo));
			executeJPushNotify(context, intent, receiverInfo);
		}
		else if (TextUtils.equals(ACTION_PUSH_CLICK, action) && receiverInfo != null) {//消息点击事件
			executeNotificationClick(context, receiverInfo);
		}
		else if (TextUtils.equals(ACTION_FROM_IM, action)) {//im的推送会被广播到这里统一处理
			executeChatMessageNotify(context, intent);
		}
		else if (TextUtils.equals(ACTION_PUSH_NOTIFICATION, action)) {//极光VIP通道，都要走通知消息
			assert receiverInfo != null;
			executeJPushNotify(context, receiverInfo);
		}
		else if (TextUtils.equals(ACTION_PUSH_TOEN, action)) {//收到手机唯一值会广播这里，提醒主界面更新token
			EventBus.getDefault().post(new EventHuaweiPushInitSuccess());
		}
	}

	private void executeJPushNotify(Context context, Intent intent, ReceiverInfo receiverInfo) {
		NotificationMessage message = formatJson(receiverInfo.extra);
		if (message == null) return;
		int badge = message.getBadge();
		if (IMHuanXinHelper.getInstance().isImLogin()) {
			badge += IMHuanXinHelper.getInstance().getUnreadCount();
		}

		FEApplication feApplication = (FEApplication) context.getApplicationContext();
		feApplication.setCornerNum(badge);
		BadgeUtil.setBadgeCount(context, badge);                 // 设置桌面角标
		if (!"xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {    // 不是小米的话就发个角标，小米角标已经发送到通知栏了
			sendJPushNotification(context, message, intent, receiverInfo.content);
		}
	}

	private void executeJPushNotify(Context context, ReceiverInfo receiverInfo) {
		NotificationMessage message = formatJson(receiverInfo.extra);
		if (message == null) return;
		int badge = message.getBadge();
		if (IMHuanXinHelper.getInstance().isImLogin()) {
			badge += IMHuanXinHelper.getInstance().getUnreadCount();
		}

		FEApplication feApplication = (FEApplication) context.getApplicationContext();
		feApplication.setCornerNum(badge);
		BadgeUtil.setBadgeCount(context, badge);                 // 设置桌面角标
	}

	private void executeNotificationClick(Context context, ReceiverInfo receiverInfo) {
		NotificationMessage message = formatJson(receiverInfo.extra);
		if (message == null) return;
		if (!NotificationController.startDetailActivity(context, message)) {
			String content = receiverInfo.content;
			FEToast.showMessage(content);
		}
	}

	private void executeChatMessageNotify(Context context, Intent intent) {
		String conversationId = intent.getStringExtra("conversationId");
		String notifyText = intent.getStringExtra("notifyText");
		String sendName = intent.getStringExtra("notifyTitle");     // 1.single 2.group
		String notifyContext = TextUtils.isEmpty(sendName) ? notifyText : sendName + ":" + notifyText;

		int messageType = intent.getIntExtra("messageType", 1);     // 1.single 2.group
		int notifyId = getNotifyId(conversationId);

		Intent openIntent = getChatIntent(conversationId, messageType);
		intent.setPackage("cn.flyrise.study");
		PendingIntent pdIntent = PendingIntent.getActivity(context, notifyId, openIntent, FLAG_UPDATE_CURRENT);

		Notification notification = FeepPushManager.createSilenceNotification(context, NOTIFY_TITLE, notifyContext,
				notifyContext, System.currentTimeMillis(), pdIntent, R.drawable.icon,
				R.drawable.icon, R.color.app_icon_bg, true);
		if (notification == null) return;
		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		if ("xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {                // 小米角标需要发送通知
			try {
				int oaNotification = SpUtil.get("notification_badge", 0);   // 获取 OA 未读消息数
				Field field = notification.getClass().getDeclaredField("extraNotification");
				Object extraNotification = field.get(notification);
				Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
				method.invoke(extraNotification, oaNotification + 1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			notifyManager.cancel(600);
			notifyManager.notify(600, notification);
			return;
		}

		notifyManager.notify(notifyId, notification);
		updateNotifyId(conversationId);
	}

	private void sendJPushNotification(Context context, NotificationMessage message, Intent intent, String content) {
		if (message == null) return;
		String lastUserId = getLastUserId();
		String userId = message.getUserId();
		if (!TextUtils.equals(lastUserId, userId)) return;
		EventBus.getDefault().post(new EventJPushRefreshMessage());        // 当有推送过来的时候，刷新最新消息界面

		int notifyId = getNotifyId(null);
		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		intent.setAction(ACTION_PUSH_CLICK);
		PendingIntent pdIntent = PendingIntent.getBroadcast(context, notifyId, intent, FLAG_UPDATE_CURRENT);
		Notification notification = FeepPushManager.createNotification(context, NOTIFY_TITLE,
				content, null, 0, pdIntent,
				R.drawable.icon,
				R.drawable.icon,
				R.color.app_icon_bg, true);
		if (notification == null) return;
		notifyManager.notify(notifyId, notification);
		updateNotifyId(null);
	}

	private Intent getChatIntent(String conversationId, int messageType) {
		boolean isChatActivity = SpUtil.get("notification_acitivity", false);
		Class<? extends Activity> targetClass = isChatActivity ? ChatActivity.class : SplashActivity.class;
		Context activity = CoreZygote.getApplicationServices().getFrontActivity();
		Intent intent;
		if (activity != null) {
			intent = new Intent(activity, targetClass);
		}
		else {
			intent = new Intent(CoreZygote.getContext(), targetClass);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		intent.putExtra(EaseUiK.EmChatContent.emChatType,
				messageType == 1 ? EaseUiK.EmChatContent.em_chatType_single : EmChatContent.em_chatType_group);
		intent.putExtra(EaseUiK.EmChatContent.emChatID, conversationId);
		return intent;
	}

	private NotificationMessage formatJson(String jsonString) {
		try {
			return GsonUtil.getInstance().fromJson(jsonString, NotificationMessage.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getLastUserId() {//上一次登陆用户
		String userId = "";
		try {
			userId = UserInfoTableUtils.find().getUserID();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return userId;
	}

	private ReceiverInfo getReceiverInfo(Intent intent) {
		try {
			return new ReceiverInfo.Builder()
					.setContent(intent.getStringExtra("PUSH_CONTENT"))
					.setExtra(intent.getStringExtra("PUSH_EXTRA"))
					.create();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private int getNotifyId(String conversationId) {
		if (!TextUtils.isEmpty(conversationId)) {
			Integer notifyId = sNotifyIdMap.get(conversationId);
			if (notifyId != null) return notifyId.intValue();
		}
		return sNotificationId;
	}

	private void updateNotifyId(String conversationId) {
		if (!TextUtils.isEmpty(conversationId)) {
			Integer notifyId = sNotifyIdMap.get(conversationId);
			if (notifyId == null) {
				sNotifyIdMap.put(conversationId, sNotificationId);
				sNotificationId++;
				if (sNotificationId >= NOTIFICATION_NUMS) sNotificationId = 0;
			}
			return;
		}

		sNotificationId++;
		if (sNotificationId >= NOTIFICATION_NUMS) sNotificationId = 0;
	}
}
