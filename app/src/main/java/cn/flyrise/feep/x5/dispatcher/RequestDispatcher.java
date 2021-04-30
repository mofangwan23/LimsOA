package cn.flyrise.feep.x5.dispatcher;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.request.NoticesManageRequest;
import cn.flyrise.feep.x5.Request;
import java.util.Arrays;

/**
 * @author 社会主义接班人
 * @since 2018-09-17 16:23
 */
public abstract class RequestDispatcher {

	protected final Request request;
	protected final String userId;
	protected final String serverHost;

	public RequestDispatcher(Request request) {
		this.request = request;
		this.userId = CoreZygote.getLoginUserServices().getUserId();
		this.serverHost = CoreZygote.getLoginUserServices().getServerAddress();
	}

	public abstract boolean needIntercept();

	public abstract void doIntercept(Context context);

	public abstract String getHomeLink();

	protected void markMessageReaded(String messageId) {
		NoticesManageRequest request = new NoticesManageRequest();
		request.setMsgIds(Arrays.asList(messageId));
		request.setUserId(userId);
		FEHttpClient.getInstance().post(request, null);
	}

	protected String verifyRemoteURL(String url) {
		if (TextUtils.isEmpty(url)) return url;
		if (url.startsWith("http") || url.startsWith("https")) return url;
		return serverHost + url;
	}

}
