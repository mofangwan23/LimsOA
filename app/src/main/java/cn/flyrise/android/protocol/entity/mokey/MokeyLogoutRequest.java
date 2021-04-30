package cn.flyrise.android.protocol.entity.mokey;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by klc on 2018/3/13.
 * 手机盾扫码获得挑战数据获得accToken返回给服务端的请求。
 */

public class MokeyLogoutRequest extends RequestContent {

	private final String NAMESPACE = "MobileKeyRequest";
	public final String method = "MobileKeyCancel";

	private String pwd;

	public MokeyLogoutRequest(String pwd) {
		this.pwd = pwd;
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}
}

