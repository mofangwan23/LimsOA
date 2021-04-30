package cn.flyrise.feep.push.target.jiguang;

import android.app.Application;
import android.content.Context;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.push.Push.Phone;
import cn.flyrise.feep.push.PushBaseContact;
import cn.jpush.android.api.JPushInterface;

/**
 * Create by cm132 on 2019/5/16 10:19.
 * Describe:极光
 */
public class JPushInit extends PushBaseContact {

	public JPushInit(Application application) {
		JPushInterface.init(application);
	}

	@Override public void resumePush(Context context) {
		JPushInterface.resumePush(context);
	}

	@Override public void stopPush(Context context) {
		JPushInterface.stopPush(context);
	}

	@Override public void deleteAlias(Context context) {
		SpUtil.put(Phone.jpush, "");
		JPushInterface.deleteAlias(context, 1011);
	}

	@Override public String getAppId(Context context) {
		return "";
	}

	@Override protected String getAppKey(Context context) {
		return "";
	}

	@Override protected void getAlias(Context context) {
		JPushInterface.getAlias(context, 0);
	}
}
