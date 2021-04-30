package cn.flyrise.feep.x5.dispatcher;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.x5.Request;

/**
 * @author 社会主义接班人
 * @since 2018-09-18 10:02
 */
public class HeadLineDispatcher extends RequestDispatcher {

	private final Module headline;

	public HeadLineDispatcher(Request request) {
		super(request);
		this.headline = FunctionManager.findModule(Func.Headline);
	}

	@Override public boolean needIntercept() {
		return false;
	}

	@Override public void doIntercept(Context context) {
		// Unnecessary to implementation.
	}

	@Override public String getHomeLink() {
		String url = headline.url;
		if (TextUtils.isEmpty(url)) {
			url = "/mdp/html/yunger/listUI.html";
		}

		if (!TextUtils.isEmpty(request.businessId)) {
			url = url + "?activeid=" + request.businessId;
		}
		return verifyRemoteURL(url);
	}
}
