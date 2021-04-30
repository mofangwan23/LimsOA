package cn.flyrise.feep.push.target.jiguang;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.push.Push.Notification;
import cn.flyrise.feep.push.Push.Phone;
import cn.flyrise.feep.push.PushReceiverHandleManager;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * 自定义的alin/tag设置接口回调
 * Created by luoming on 2018/5/28.
 */

public class IJPushEventReceiver extends JPushMessageReceiver {

	@Override
	public void onTagOperatorResult(Context var1, JPushMessage var2) {
	}

	@Override
	public void onCheckTagOperatorResult(Context var1, JPushMessage var2) {

	}

	@Override
	public void onAliasOperatorResult(Context context, JPushMessage message) {
		if (message.getErrorCode() != 0) return;//用户注册SDK的intent
		FELog.i("push-handle:result-getalias():" + message.getAlias());
		String jpushToken = SpUtil.get(Phone.jpush, "");
		if (!TextUtils.isEmpty(jpushToken) && TextUtils.equals(jpushToken, CoreZygote.getDevicesToken())) {
			return;
		}
		PushReceiverHandleManager.getInstance().onAliasSet(context, Notification.JPUSH, "");
	}

	@Override
	public void onMobileNumberOperatorResult(Context var1, JPushMessage var2) {

	}
}
