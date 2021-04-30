package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author 社会主义接班人
 * @since 2018-07-27 15:17
 */
public class SwitchCompanyRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "AppsRequest";
	}

	public String method;
	public String orgId;

	public SwitchCompanyRequest() { }

	public SwitchCompanyRequest(String orgId) {
		this.method = "switchCo";
		this.orgId = orgId;
	}
}
