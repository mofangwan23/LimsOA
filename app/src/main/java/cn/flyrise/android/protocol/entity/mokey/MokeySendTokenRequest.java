package cn.flyrise.android.protocol.entity.mokey;

import cn.flyrise.feep.core.network.request.RequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;
import com.google.gson.annotations.SerializedName;

/**
 * Created by klc on 2018/3/13.
 * 手机盾扫码获得挑战数据获得accToken返回给服务端的请求。
 */

public class MokeySendTokenRequest extends RequestContent {

	private final String NAMESPACE = "MobileKeyRequest";
	public final String method = "MobileKeyResponse";

	private String eventData;
	private String accToken;

	public MokeySendTokenRequest(String eventData, String accToken) {
		this.eventData = eventData;
		this.accToken = accToken;
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}
}

