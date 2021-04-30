package cn.flyrise.feep.x5.dispatcher;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.x5.Request;

/**
 * @author 社会主义接班人
 * @since 2018-09-18 09:54
 */
public class CRMDispatcher extends RequestDispatcher {

	private final Module crm;

	public CRMDispatcher(Request request) {
		super(request);
		this.crm = FunctionManager.findModule(Func.CRM);
	}

	@Override public boolean needIntercept() {
		return false;
	}

	@Override public void doIntercept(Context context) {
		// Unnecessry to implementation.
	}

	@Override public String getHomeLink() {
		String url = crm.url;
		if (TextUtils.isEmpty(url)) {
			url = "/mdp/html/CRM/index.html";
		}

		if (!TextUtils.isEmpty(request.businessId)) {
			url = url + "?activeid=" + request.businessId;
		}
		return verifyRemoteURL(url);
	}
}
