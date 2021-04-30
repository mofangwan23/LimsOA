package cn.flyrise.android.protocol.entity.mokey;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by klc on 2018/3/13.
 * 手机盾重置事件（长码）
 */

public class MokeyResetEventDataRequest extends RequestContent {

	public final String method = "MobileKeyStringReset";

	private String pwd;

	public MokeyResetEventDataRequest(String pwd) {
		this.pwd = pwd;
	}

	@Override
	public String getNameSpace() {
		return "MobileKeyRequest";
	}
}

