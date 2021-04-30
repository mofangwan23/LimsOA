package cn.flyrise.feep.push.target.xiaomi;

import android.content.Context;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.notification.ReceiverInfo;
import cn.flyrise.feep.push.Push;
import cn.flyrise.feep.push.Push.Notification;
import cn.flyrise.feep.push.Push.Receiver;
import cn.flyrise.feep.push.PushReceiverHandleManager;
import com.google.gson.Gson;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;
import java.util.List;

/**
 * 自定义的小米推送接收
 * Created by luoming on 2018/5/28.
 */

public class XiaomiBroadcastReceiver extends PushMessageReceiver {

//	private PluginXiaomiPlatformsReceiver jiguangReceiver = null;//极光推送sdk小米广播

	@Override public void onCommandResult(Context context, MiPushCommandMessage message) {
		super.onCommandResult(context, message);
//		if(jiguangReceiver==null)jiguangReceiver=new PluginXiaomiPlatformsReceiver();
//		jiguangReceiver.onCommandResult(context, message);
		String command = message.getCommand();
		List<String> arguments = message.getCommandArguments();
		if (MiPushClient.COMMAND_REGISTER.equals(command) && message.getResultCode() == ErrorCode.SUCCESS) {
			PushReceiverHandleManager.getInstance().onAliasSet(context, Notification.XIAOMI, arguments.get(0));
		}
	}

	@Override
	public void onReceiveRegisterResult(Context var1, MiPushCommandMessage var2) {
//		if(jiguangReceiver==null)jiguangReceiver=new PluginXiaomiPlatformsReceiver();
//		jiguangReceiver.onReceiveRegisterResult(var1, var2);
		PushReceiverHandleManager.getInstance().onRegistration(Push.Notification.XIAOMI, "小米推送注册成功");
	}

	@Override
	public void onReceivePassThroughMessage(Context var1, MiPushMessage var2) {
//		if(jiguangReceiver==null)jiguangReceiver=new PluginXiaomiPlatformsReceiver();
//		jiguangReceiver.onReceivePassThroughMessage(var1, var2);
		PushReceiverHandleManager.getInstance().onMessageReceived(var1, convert2ReceiverInfo(var2, Receiver.message));
	}

	@Override
	public void onNotificationMessageClicked(Context var1, MiPushMessage var2) {
//		if(jiguangReceiver==null)jiguangReceiver=new PluginXiaomiPlatformsReceiver();
//		jiguangReceiver.onNotificationMessageClicked(var1, var2);
		FELog.i("push-handle:小米：通知栏点击");
		PushReceiverHandleManager.getInstance().onMessageReceived(var1, convert2ReceiverInfo(var2, Receiver.click));
	}

	@Override
	public void onNotificationMessageArrived(Context var1, MiPushMessage var2) {
//		if(jiguangReceiver==null)jiguangReceiver=new PluginXiaomiPlatformsReceiver();
//		jiguangReceiver.onNotificationMessageArrived(var1, var2);
		FELog.i("push-handle:小米：messageArrived");
		PushReceiverHandleManager.getInstance().onMessageReceived(var1, convert2ReceiverInfo(var2, Receiver.notification));
	}

	/**
	 * 将intent的数据转化为ReceiverInfo用于处理
	 */
	private ReceiverInfo convert2ReceiverInfo(MiPushMessage miPushMessage, int infoType) {
		return new ReceiverInfo.Builder()
				.setContent(miPushMessage.getContent())
				.setPushTarget(Notification.XIAOMI)
				.setTitle(miPushMessage.getTitle())
				.setRawData(miPushMessage)
				.setInfoType(infoType)
				.setExtra(miPushMessage.getExtra() != null ? new Gson().toJson(miPushMessage.getExtra()) : null)
				.create();
	}
}
