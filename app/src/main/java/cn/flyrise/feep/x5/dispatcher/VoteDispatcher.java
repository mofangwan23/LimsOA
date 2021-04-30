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
 * @since 2018-09-18 09:23
 */
public class VoteDispatcher extends RequestDispatcher {

	private final Module vote;

	public VoteDispatcher(Request request) {
		super(request);
		this.vote = FunctionManager.findModule(Func.Vote);
	}

	@Override public boolean needIntercept() {
		return TextUtils.isEmpty(vote.url);
	}

	@Override public void doIntercept(Context context) {
		FRouter.build(context, "/cordova/old/page")
				.withString("homeLink", getHomeLink())
				.go();
	}

	@Override public String getHomeLink() {
		if (TextUtils.isEmpty(vote.url)) {
			String url = "file:///android_asset/wechat/html/vote/vote-page.html";
			if (!TextUtils.isEmpty(request.businessId)) {
				url = url + "#pageVoteDetail?id=" + request.businessId;
			}
			return url;
		}

		String url = vote.url;
		if (!TextUtils.isEmpty(request.businessId)) {
			url = url + "?activeid=" + request.businessId;
		}
		return verifyRemoteURL(url);
	}
}
