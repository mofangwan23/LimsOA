package cn.flyrise.feep.push.target.huawei;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.notification.ReceiverInfo;
import cn.flyrise.feep.push.Push;
import cn.flyrise.feep.push.Push.Notification;
import cn.flyrise.feep.push.Push.Receiver;
import cn.flyrise.feep.push.PushReceiverHandleManager;
import com.huawei.hms.support.api.push.PushReceiver;

/**
 * 自定义的华为推送服务的接收器
 * Created by luoming on 2018/5/29.
 */

public class HuaweiPushBroadcastReceiver extends PushReceiver {

	private static final String TAG = "HuaweiPushBroadcastRece";
	private Handler mHandler = new Handler();
//	private PluginHuaweiPlatformsReceiver receiver;

	//token获取完成；token用于标识设备
	@Override
	public void onToken(Context context, String token, Bundle extras) {//获取token成功，token用于标识设备的唯一性
//		if(receiver==null)receiver=new PluginHuaweiPlatformsReceiver();
//		receiver.onToken(context,token,extras);
		PushReceiverHandleManager.getInstance().onAliasSet(context, Notification.HUAWEI, token);
	}

	//接收到了穿透消息
	@Override
	public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {//CP可以自己解析消息内容，然后做相应的处理
//		if(receiver==null)receiver=new PluginHuaweiPlatformsReceiver();
//		receiver.onPushMsg(context,msg,bundle);
//		FELog.i("push-handle:华为：透传消息");
//		try {
//			PushReceiverHandleManager.getInstance().onMessageReceived(context
//					, new ReceiverInfo.Builder()
//							.setContent(new String(msg, "UTF-8"))
//							.setRawData(bundle)
//							.setPushTarget(Push.Notification.HUAWEI)
//							.setInfoType(Receiver.message)
//							.create());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return false;
	}

	// 点击通知栏事件处理
	@Override
	public void onEvent(final Context context, Event event, final Bundle bundle) {//点击通知事件
//		if(receiver==null)receiver=new PluginHuaweiPlatformsReceiver();
//		receiver.onEvent(context,event,bundle);
		FELog.i("push-handle:华为：通知栏点击");
		try {
			mHandler.postDelayed(new Runnable() {
				@Override public void run() {
					PushReceiverHandleManager.getInstance().onMessageReceived(context
							, new ReceiverInfo.Builder().setRawData(bundle)
									.setPushTarget(Notification.HUAWEI)
									.setInfoType(Receiver.click)
									.create());
				}
			}, 1200);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//SDK状态
	@Override
	public void onPushState(Context context, boolean pushState) {
//		if(receiver==null)receiver=new PluginHuaweiPlatformsReceiver();
//		receiver.onPushState(context,pushState);
		if (pushState) {
			PushReceiverHandleManager.getInstance().onRegistration(Push.Notification.HUAWEI, "华为推送注册成功");
		}
	}

}
