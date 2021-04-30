package cn.flyrise.feep.push;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.huawei.android.hms.agent.HMSAgent;
import cn.flyrise.feep.push.Push.Notification;
import cn.flyrise.feep.push.Push.Phone;
import cn.flyrise.feep.push.target.huawei.HuaweiInit;
import cn.flyrise.feep.push.target.jiguang.JPushInit;
import cn.flyrise.feep.push.target.xiaomi.XiaomiInit;
import cn.jpush.android.api.JPushInterface;
import com.xiaomi.mipush.sdk.MiPushClient;

/**
 * Create by cm132 on 2019/5/17 16:14.
 * Describe:
 */
public class PushTargetManager {

	private PushBaseContact jPush;
	private PushBaseContact basePush;

	private static class SingletonHolder {

		private static final PushTargetManager INSTANCE = new PushTargetManager();
	}

	public static PushTargetManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	//小米的使用小米推送，华为使用华为推送，其他的使用极光推送
	public void init(Application context) {
		String mobileBrand = android.os.Build.MANUFACTURER;
		FELog.i("push-manufacturer:" + mobileBrand);
		JPushInterface.setDebugMode(true);
		jPush = new JPushInit(context);
		if (TextUtils.equals(Phone.huawei, mobileBrand)) {
			basePush = new HuaweiInit(context);
		}
		else if (TextUtils.equals(Phone.xiaomi, mobileBrand)) {
			basePush = new XiaomiInit(context);
		}
		else {
			basePush = null;
		}
		NiuBiNotification.notificationChannel(context);
	}

	public void reInitAllalias() {//重新获取推送
		String mobileBrand = android.os.Build.MANUFACTURER;
		if (TextUtils.equals(mobileBrand, Phone.huawei)) { //华为
			HMSAgent.Push.getToken(null);
		}
		else if (TextUtils.equals(mobileBrand, Phone.xiaomi)) {
			MiPushClient.getAllAlias(CoreZygote.getContext());
		}
		setJpushAlias();
	}

	public String getCurrentToken() {
		String mobileBrand = android.os.Build.MANUFACTURER;
		String token = "";
		if (TextUtils.equals(Phone.huawei, mobileBrand)) {
			token = SpUtil.get(Push.Phone.huawei, "");
		}
		else if (TextUtils.equals(Phone.xiaomi, mobileBrand)) {
			token = SpUtil.get(Push.Phone.xiaomi, "");
		}

		return TextUtils.isEmpty(token) ? CoreZygote.getDevicesToken() : token;
	}

	public String getAppId() {
		return basePush == null ? "" : basePush.getAppId(CoreZygote.getContext());
	}


	public String getAppkey() {
		return basePush == null ? "" : basePush.getAppKey(CoreZygote.getContext());
	}


	//设置是否启用消息推送功能
	public void setNotificationOpen(Boolean isStartNotification) {
		if (isStartNotification) {
			if (jPush != null) jPush.resumePush(CoreZygote.getContext());
			if (basePush != null) basePush.resumePush(CoreZygote.getContext());
		}
		else {
			if (jPush != null) jPush.stopPush(CoreZygote.getContext());
			if (basePush != null) basePush.stopPush(CoreZygote.getContext());
		}
	}

	public void deleteAlias() {//注销清空别买
		if (jPush != null) jPush.deleteAlias(CoreZygote.getContext());
		if (basePush != null) basePush.deleteAlias(CoreZygote.getContext());
	}

	public void getAlias(Context context) {
		if (jPush != null) jPush.getAlias(context);
	}

	private void setJpushAlias() {
		PushReceiverHandleManager.getInstance().onAliasSet(CoreZygote.getContext(), Notification.JPUSH, "");
	}
}
