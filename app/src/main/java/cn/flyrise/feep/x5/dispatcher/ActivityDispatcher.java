package cn.flyrise.feep.x5.dispatcher;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.x5.Request;
import cn.squirtlez.frouter.FRouter;

/**
 * @author 社会主义接班人
 * @since 2018-09-18 09:45
 */
public class ActivityDispatcher extends RequestDispatcher {

	private final Module activity;

	public ActivityDispatcher(Request request) {
		super(request);
		this.activity = FunctionManager.findModule(Func.Activity);
	}

	@Override public boolean needIntercept() {
		return activity == null || TextUtils.isEmpty(activity.url);
	}

	@Override public void doIntercept(Context context) {
		FRouter.build(context, "/cordova/old/page")
				.withString("homeLink", getHomeLink())
				.go();
	}

	@Override public String getHomeLink() {
		if (TextUtils.isEmpty(activity.url)) {
			String url = "file:///android_asset/wechat/html/activity/activity_list.html";
			if (!TextUtils.isEmpty(request.businessId)) {
				url = url + "#editActivity?id=" + request.businessId;
			}
			return url;
		}

		String url = activity.url;
		if (!TextUtils.isEmpty(request.businessId)) {
			url = url + "?activeid=" + request.businessId;
		}
		return verifyRemoteURL(url);
	}
}
