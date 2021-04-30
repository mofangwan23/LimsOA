package cn.flyrise.feep.push.target.xiaomi;

import android.app.Application;
import android.content.Context;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.push.BuildConfig;
import cn.flyrise.feep.push.MainifestUtil;
import cn.flyrise.feep.push.PushBaseContact;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

public class XiaomiInit extends PushBaseContact {

	public void resumePush(Context context) {
		MiPushClient.resumePush(context, null);
	}

	public void stopPush(Context context) {
		MiPushClient.pausePush(context, null);
	}

	@Override
	public void deleteAlias(Context context) {

	}

	@Override public String getAppId(Context context) {
		return MainifestUtil.getXiaoMiAppid(context);
	}

	@Override
	protected String getAppKey(Context context) {
		return MainifestUtil.getXiaoMiAppKey(context);
	}

	@Override protected void getAlias(Context context) {
	}

	public XiaomiInit(Application context) {
		//调试
		if (BuildConfig.DEBUG) {
			Logger.setLogger(context, new LoggerInterface() {
				@Override public void setTag(String s) {
					FELog.i("push-handle:tag: $tag+" + s);
				}

				@Override public void log(String s) {
					FELog.i("push-handle:content:--Throwable:" + s);
				}

				@Override public void log(String s, Throwable throwable) {
					FELog.i("push-handle:log: $content:" + throwable.getMessage());
				}
			});
		}

		FELog.i("push-xiaomi--id>>>>:" + MainifestUtil.getXiaoMiAppid(context));
		FELog.i("push-xiaomi--key>>>>:" + MainifestUtil.getXiaoMiAppKey(context));

		//注册SDK
		MiPushClient.registerPush(context, getAppId(context), getAppKey(context));
	}
}
