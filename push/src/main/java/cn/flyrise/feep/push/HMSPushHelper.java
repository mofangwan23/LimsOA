package cn.flyrise.feep.push;

import android.app.Application;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.huawei.android.hms.agent.HMSAgent;
import cn.flyrise.feep.huawei.android.hms.agent.common.handler.ConnectHandler;
import cn.flyrise.feep.huawei.android.hms.agent.push.handler.GetTokenHandler;
import java.lang.reflect.Method;

/**
 * Created by lzan13 on 2018/4/10.
 */

public class HMSPushHelper {

	private static HMSPushHelper instance;

	// 是否使用华为 hms
	private boolean isUseHMSPush = false;

	private HMSPushHelper() {}

	public static HMSPushHelper getInstance() {
		if (instance == null) {
			instance = new HMSPushHelper();
		}
		return instance;
	}

	/**
	 * 初始化华为 HMS 推送服务
	 */
	public boolean initHMSAgent(Application application) {
		try {
			if (Class.forName("com.huawei.hms.support.api.push.HuaweiPush") != null) {
				Class<?> classType = Class.forName("android.os.SystemProperties");
				Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
				String buildVersion = (String) getMethod.invoke(classType, new Object[]{"ro.build.version.emui"});
				//在某些手机上，invoke方法不报错
				if (!TextUtils.isEmpty(buildVersion)) {
					FELog.d("HWHMSPush", "huawei hms push is available!");
					isUseHMSPush = true;
					return HMSAgent.init(application);
				}
				else {
					FELog.d("HWHMSPush", "huawei hms push is unavailable!");
				}
			}
			else {
				FELog.d("HWHMSPush", "no huawei hms push sdk or mobile is not a huawei phone");
			}
		} catch (Exception e) {
			FELog.d("HWHMSPush", "no huawei hms push sdk or mobile is not a huawei phone");
		}
		return false;
	}

	/**
	 * 连接华为移动服务
	 */
	public void connectHMS() {
		if (isUseHMSPush) {
			HMSAgent.connect(new ConnectHandler() {
				@Override
				public void onConnect(int rst) {
					FELog.d("HWHMSPush", "huawei hms push connect result code:" + rst);
				}
			});
		}
	}

	/**
	 * 获取华为推送
	 * 注册华为推送 token 通用错误码列表
	 * http://developer.huawei.com/consumer/cn/service/hms/catalog/huaweipush_agent.html?page=hmssdk_huaweipush_api_reference_errorcode
	 */
	public void getHMSPushToken() {
		if (isUseHMSPush) {
			HMSAgent.Push.getToken(new GetTokenHandler() {
				@Override
				public void onResult(int rst) {
					FELog.d("HWHMSPush", "get huawei hms push token result code:" + rst);
				}
			});
		}
	}

	public boolean isUseHMSPush() {
		return isUseHMSPush;
	}

}
