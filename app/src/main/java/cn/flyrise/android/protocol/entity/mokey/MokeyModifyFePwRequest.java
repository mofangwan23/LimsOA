package cn.flyrise.android.protocol.entity.mokey;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by klc on 2018/3/13.
 * 手机盾扫码获得挑战数据获得accToken返回给服务端的请求。
 */

public class MokeyModifyFePwRequest extends RequestContent {

	private final String NAMESPACE = "MobileKeyRequest";
	public final String method = "UpdatePassWord";

	private String oldPwd;
	private String newPwd;

	public MokeyModifyFePwRequest(String oldPwd, String newPwd) {
		this.oldPwd = oldPwd;
		this.newPwd = newPwd;
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}
}

