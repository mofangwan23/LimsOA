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
 * @since 2018-09-17 16:31
 */
public class KnowledgeDispatcher extends RequestDispatcher {

	private final Module knowledge;

	public KnowledgeDispatcher(Request request) {
		super(request);
		this.knowledge = FunctionManager.findModule(Func.Knowledge);
		if (!TextUtils.isEmpty(request.messageId)) {
			markMessageReaded(request.messageId);
		}
	}

	@Override public boolean needIntercept() {
		// 这两种情况下进行拦截
		return FunctionManager.isNative(Func.Knowledge) || TextUtils.isEmpty(knowledge.url);
	}

	@Override public void doIntercept(Context context) {
		if (isOldestVersion()) {
			FRouter.build(context, "/cordova/old/page")
					.withString("homeLink", getHomeLink())
					.go();
			return;
		}

		if (TextUtils.isEmpty(request.businessId) && TextUtils.isEmpty(request.messageId)) {
			FRouter.build(context, "/knowledge/native/home").go();
			return;
		}

		if (TextUtils.isEmpty(request.businessId) && !TextUtils.isEmpty(request.messageId)) {
			FRouter.build(context, "/knowledge/native/RecFileFromMsg")
					.withString("EXTRA_RECEIVERMSAID", request.messageId)
					.go();
			return;
		}

		FRouter.build(context, "/knowledge/native/FileDetail")
				.withString("fileId", request.businessId)
				.go();
	}

	@Override public String getHomeLink() {
		if (TextUtils.isEmpty(knowledge.url)) {
			String url = "file:///android_asset/wechat/html/knowledge/km.html";
			if (!TextUtils.isEmpty(request.businessId)) {
				url = url + "?id=" + request.businessId;
			}
			return url;
		}

		String url = knowledge.url;
		if (!TextUtils.isEmpty(request.businessId)) {
			url = url + "?activeid=" + request.businessId;
		}
		return verifyRemoteURL(url);
	}

	private boolean isOldestVersion() {
		return !FunctionManager.isNative(Func.Knowledge) && TextUtils.isEmpty(knowledge.url);
	}
}
