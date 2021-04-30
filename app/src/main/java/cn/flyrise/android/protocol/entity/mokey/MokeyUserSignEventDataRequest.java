package cn.flyrise.android.protocol.entity.mokey;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by klc on 2018/3/13.
 * 手机盾重置事件（长码）
 */

public class MokeyUserSignEventDataRequest extends RequestContent {

	public final String method = "UserSignEventData";

	@Override
	public String getNameSpace() {
		return "MobileKeyRequest";
	}
}

