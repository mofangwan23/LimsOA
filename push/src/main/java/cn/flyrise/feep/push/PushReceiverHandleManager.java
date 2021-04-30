package cn.flyrise.feep.push;

import android.content.Context;
import cn.flyrise.feep.core.notification.ReceiverInfo;
import cn.flyrise.feep.push.handle.HandleReceiverMessage;
import cn.flyrise.feep.push.handle.HandleReceiverRegistration;

/**
 * 统一处理收到的推送
 * Created by luoming on 2018/5/28.
 */

public class PushReceiverHandleManager {

	private static PushReceiverHandleManager instance;

	private HandleReceiverMessage mMessageHandle;
	private HandleReceiverRegistration mSDKRegistrationHandle;

	public static PushReceiverHandleManager getInstance() {
		if (instance == null) {
			synchronized (PushReceiverHandleManager.class) {
				if (instance == null) {
					instance = new PushReceiverHandleManager();
				}
			}
		}
		return instance;
	}

	private PushReceiverHandleManager() {

	}

	//用户注册sdk之后的通知
	public void onRegistration(int type, String content) {
		if (mSDKRegistrationHandle == null) mSDKRegistrationHandle = new HandleReceiverRegistration();
		mSDKRegistrationHandle.onRegistration(type, content);
	}

	//设置了别名之后
	public void onAliasSet(Context context, int type, String token) {
		if (mSDKRegistrationHandle == null) mSDKRegistrationHandle = new HandleReceiverRegistration();
		mSDKRegistrationHandle.onAliasSet(context, type, token);
	}

	//接收到消息推送，不会主动显示在通知栏
	public void onMessageReceived(Context context, ReceiverInfo info) {
		if (mMessageHandle == null) mMessageHandle = new HandleReceiverMessage();
		mMessageHandle.handle(context, info);
	}
}
