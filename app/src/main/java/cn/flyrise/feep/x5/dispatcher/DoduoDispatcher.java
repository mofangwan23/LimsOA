package cn.flyrise.feep.x5.dispatcher;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.x5.Request;

/**
 * @author 社会主义接班人
 * @since 2018-09-18 10:06
 */
public class DoduoDispatcher extends RequestDispatcher {

	private final Module doduo;

	public DoduoDispatcher(Request request) {
		super(request);
		this.doduo = FunctionManager.findModule(Func.Dudu);
	}

	@Override public boolean needIntercept() {
		return false;
	}

	@Override public void doIntercept(Context context) {
		// Unnecessary to implementation
	}

	@Override public String getHomeLink() {
		String url = doduo.url;
		if (TextUtils.isEmpty(url)) {
			url = "/common/dudu/callMeeting.html";
		}

		if (!TextUtils.isEmpty(request.businessId)) {
			url = url + "?activeid=" + request.businessId;
		}

		String queryParams = "";
		if (!TextUtils.isEmpty(request.extra)) {
			queryParams = TextUtils.isEmpty(request.businessId)
					? "?callNumbers=" + request.extra + "&needback=0"
					: "&callNumbers=" + request.extra + "&needback=0";
		}
		return verifyRemoteURL(url + queryParams);
	}
}
