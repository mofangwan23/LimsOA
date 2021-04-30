package cn.flyrise.feep.push.target.jiguang;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.notification.ReceiverInfo;
import cn.flyrise.feep.push.Push.Notification;
import cn.flyrise.feep.push.Push.Receiver;
import cn.flyrise.feep.push.PushReceiverHandleManager;
import cn.jpush.android.api.JPushInterface;

/**
 * 自定义的极光推送接收
 * Created by luoming on 2018/5/28.
 */

public class JPushBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) {
			return;
		}

		String action = intent.getAction();
		if (TextUtils.isEmpty(action)) {
			return;
		}

		if ("cn.jpush.android.intent.REGISTRATION".equals(action)) {//用户注册SDK的intent
			FELog.i("push-handle:result-RegistrationID():" + JPushInterface.getRegistrationID(context));
			PushReceiverHandleManager.getInstance().onRegistration(Notification.JPUSH, "极光推送注册成功");
			PushReceiverHandleManager.getInstance().onAliasSet(context, Notification.JPUSH, JPushInterface.getRegistrationID(context));
		}
		else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(action)) {
			FELog.i("push-handle:极光：message_received");
			//用户接收SDK消息的intent
			PushReceiverHandleManager.getInstance().onMessageReceived(context, convert2MessageReceiverInfo(intent, Receiver.message));
		}
		else if ("cn.jpush.android.intent.NOTIFICATION_RECEIVED".equals(action)) {
			FELog.i("push-handle:极光：notification_received");
			//用户接收SDK通知栏信息的intent
			PushReceiverHandleManager.getInstance().onMessageReceived(context, convert2MessageReceiverInfo(intent, Receiver.notification));
		}
		else if ("cn.jpush.android.intent.NOTIFICATION_OPENED".equals(action)) {
			String mobileBrand = android.os.Build.MANUFACTURER;
			FELog.i("push- :极光：notification_opened");
			//用户打开自定义通知栏的intent
			PushReceiverHandleManager.getInstance().onMessageReceived(context, convert2MessageReceiverInfo(intent, Receiver.click));
		}
	}

	/**
	 * 将intent的数据转化为ReceiverInfo用于处理
	 */
	private ReceiverInfo convert2NotificationReceiverInfo(Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle == null) return null;
		return new ReceiverInfo.Builder()
				.setTitle(bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE))
				.setContent(bundle.getString(JPushInterface.EXTRA_ALERT))
				.setExtra(bundle.getString(JPushInterface.EXTRA_EXTRA))
				.setPushTarget(Notification.JPUSH)
				.setRawData(intent)
				.create();
	}

	/**
	 * 将intent的数据转化为ReceiverInfo用于处理
	 */
	private ReceiverInfo convert2MessageReceiverInfo(Intent intent, int infoType) {
		Bundle bundle = intent.getExtras();
		if (bundle == null) return null;
		return new ReceiverInfo.Builder()
				.setTitle(bundle.getString(JPushInterface.EXTRA_TITLE))
				.setContent(bundle.getString(JPushInterface.EXTRA_MESSAGE))
				.setExtra(bundle.getString(JPushInterface.EXTRA_EXTRA))
				.setPushTarget(Notification.JPUSH)
				.setInfoType(infoType)
				.setRawData(intent)
				.create();
	}
}
