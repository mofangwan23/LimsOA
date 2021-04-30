package cn.flyrise.feep.push.target.huawei;

import android.app.Application;
import android.content.Context;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.huawei.android.hms.agent.HMSAgent;
import cn.flyrise.feep.huawei.android.hms.agent.push.handler.DeleteTokenHandler;
import cn.flyrise.feep.push.HMSPushHelper;
import cn.flyrise.feep.push.MainifestUtil;
import cn.flyrise.feep.push.Push.Phone;
import cn.flyrise.feep.push.PushBaseContact;

public class HuaweiInit extends PushBaseContact {

	public HuaweiInit(Application application) {
		if (HMSPushHelper.getInstance().initHMSAgent(application)) {
			HMSPushHelper.getInstance().connectHMS();//初始化成功获取别名
			HMSPushHelper.getInstance().getHMSPushToken();
		}
	}

	public void resumePush(Context context) {
	}

	public void stopPush(Context context) {
	}

	@Override
	public void deleteAlias(Context context) {//华为没有暂停和开启通知，所以只能删除
		HMSAgent.Push.deleteToken(SpUtil.get(Phone.huawei, ""), new DeleteTokenHandler() {
			@Override public void onResult(int rst) {
				FELog.i("push-->>>huawei-注销");
			}
		});
		SpUtil.put(Phone.huawei, "");
	}

	@Override
	public String getAppId(Context context) {
		return MainifestUtil.getHuaWeiAppid(context);
	}

	@Override
	protected String getAppKey(Context context) {
		return "";
	}

	@Override protected void getAlias(Context context) {
		HMSPushHelper.getInstance().getHMSPushToken();
	}

//	//初始化华为推送,获取别名
//	private void initHuaweiPush() {//连接成功就获取token和设置打开推送等
//		if (TextUtils.equals(PushTargetManager.huawei, android.os.Build.MANUFACTURER))
//			HMSAgent.connect(new ConnectHandler() {
//				@Override
//				public void onConnect(int rst) {
//					if (rst == HMSAgent.AgentResultCode.HMSAGENT_SUCCESS) {
//						HMSAgent.Push.getPushState(null);
//						HMSAgent.Push.getToken(null);
//					}
//				}
//			});
//	}
}
