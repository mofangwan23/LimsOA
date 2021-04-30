package cn.flyrise.feep.push.handle;

import android.content.Context;
import android.os.Bundle;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.push.Push;
import cn.flyrise.feep.push.Push.Notification;
import cn.flyrise.feep.push.Push.Phone;
import cn.jpush.android.api.JPushInterface;

/**
 * Create by cm132 on 2019/5/16 10:14.
 * Describe:注册成功和返回token值后统一回调
 */
public class HandleReceiverRegistration {

	public void onRegistration(int type, String content) {//打印注册信息
		FELog.i("push-handle:registration " + type + "--" + content);
	}

	public void onAliasSet(Context context, int type, String token) {//通知设置token
		FELog.i("push-handle:registration " + type + "--" + token);
		setAliasJPush(context, type, token);
	}


	private void setAliasJPush(Context context, int type, String token) {
		if (type == Push.Notification.JPUSH) {
			FELog.i("push-handle:alia " + type + "--" + CoreZygote.getDevicesToken());
			JPushInterface.setAlias(context, 1010, CoreZygote.getDevicesToken());   //极光使用别名推送
			SpUtil.put(Phone.jpush, token);
			JPushInterface.getAlias(context, 12);//注册完极光，获取一次，看是否注册成功，为成功，重新注册
			sendReceiver(context);
//			String mobileBrand = android.os.Build.MANUFACTURER;
//			if (TextUtils.equals(mobileBrand , Phone.huawei)) {//华为关联极光推送VIP
//				HWPushManager.doAction(context, "action_register_token", setBundle(2, SpUtil.get(Phone.huawei,"")));
//			}
//			if (TextUtils.equals(mobileBrand , Phone.xiaomi)) {//小米关联极光推送VIP
//				XMPushManager.doAction(context, "action_register_token", setBundle(1, SpUtil.get(Phone.xiaomi,"")));
//			}
		}
		if (type == Notification.HUAWEI) {//华为关联极光推送VIP
//			HWPushManager.doAction(context, "action_register_token", setBundle(2, token));
			SpUtil.put(Phone.huawei, token);
			sendReceiver(context);
		}
		if (type == Notification.XIAOMI) {//小米关联极光推送VIP
//			XMPushManager.doAction(context, "action_register_token", setBundle(1, token));
			SpUtil.put(Phone.xiaomi, token);
			sendReceiver(context);
		}
	}

	private Bundle setBundle(int platform, String token) {
		Bundle var5 = new Bundle();
		var5.putString("token", token);
		var5.putByte("platform", (byte) 1);
		return var5;
	}

	//发送这条广播，提醒首页推送token值已经更新
	private void sendReceiver(Context context) {
		if (context == null) return;
//		context.sendBroadcast(new Intent("cn.flyrise.study.notification.NotificationReceiver.PUSH_TOKEN"));
	}
}
