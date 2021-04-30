package cn.flyrise.android.protocol.entity.location;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * 新建：陈冕;
 * 日期： 2018-5-15-11:25.
 */

public class SignInStatisRequest extends RequestContent {

	public String method;

	public String month;//2018-02或者2018-02-3

	public String userId;

	public int type;//SignInStatisModel.State

	public SignInStatisRequest(String method, String month) {
		this.method = method;
		this.month = month;
	}

	public SignInStatisRequest(String method, String month, int type) {
		this.method = method;
		this.month = month;
		this.type = type;
	}


	public SignInStatisRequest(String method, String month, String userId) {
		this.method = method;
		this.month = month;
		this.userId = userId;
	}

	@Override
	public String getNameSpace() {
		return "AttendanceRequest";
	}
}
