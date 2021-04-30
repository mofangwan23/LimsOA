package cn.flyrise.feep.x5.dispatcher;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.x5.Request;

/**
 * @author 社会主义接班人
 * @since 2018-09-18 10:16
 */
public class UnknownDispatcher extends RequestDispatcher {

	private final Module unknown;

	public UnknownDispatcher(Request request) {
		super(request);
		this.unknown = FunctionManager.findModule(request.moduleId);
	}

	@Override public boolean needIntercept() {
		return false;
	}

	@Override public void doIntercept(Context context) {
		// Unnecessary to implementation.
	}

	@Override public String getHomeLink() {
		if (!TextUtils.isEmpty(request.appointURL)) {
			return verifyRemoteURL(request.appointURL);
		}
		if (unknown == null || TextUtils.isEmpty(unknown.url)) return "";
		return verifyRemoteURL(unknown.url);
	}
}
