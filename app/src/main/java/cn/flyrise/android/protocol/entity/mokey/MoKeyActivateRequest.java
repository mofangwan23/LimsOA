package cn.flyrise.android.protocol.entity.mokey;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by klc on 2018/3/13.
 * 手机盾激活请求
 */

public class MoKeyActivateRequest extends RequestContent {

	private final String NAMESPACE = "MobileKeyRequest";

	public final String method = "MobileKeyActivate";

	public  String pwd;

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

	public MoKeyActivateRequest(String pwd) {
		this.pwd = pwd;
	}
}

