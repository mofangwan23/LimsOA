package cn.flyrise.feep.x5.dispatcher;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.x5.Request;
import cn.squirtlez.frouter.FRouter;
import java.util.ArrayList;

/**
 * @author 社会主义接班人
 * @since 2018-09-17 17:23
 */
public class ScheduleDispatcher extends RequestDispatcher {

	private final Module schedule;

	public ScheduleDispatcher(Request request) {
		super(request);
		this.schedule = FunctionManager.findModule(Func.Schedule);
		if (!TextUtils.isEmpty(request.messageId)) {
			markMessageReaded(request.messageId);
		}
	}

	@Override public boolean needIntercept() {
		return FunctionManager.isNative(Func.Schedule) || TextUtils.isEmpty(schedule.url);
	}

	@Override public void doIntercept(Context context) {
		if (isOldestVersion()) {
			FRouter.build(context, "/cordova/old/page")
					.withString("homeLink", getHomeLink())
					.go();
			return;
		}

		if (TextUtils.isEmpty(request.businessId)) {
			if (CommonUtil.isEmptyList(request.userIds)) {
				FRouter.build(context, "/schedule/native").go();
				return;
			}

			FRouter.build(context, "/schedule/native/new")
					.withStringArray("userIds", (ArrayList<String>) request.userIds)
					.go();
			return;
		}

		FRouter.build(context, "/schedule/detail")
				.withString("EXTRA_EVENT_SOURCE_ID", request.businessId)
				.withString("EXTRA_SCHEDULE_ID", request.messageId)
				.withString("EXTRA_EVENT_SOURCE", "fe.do?SYS.ACTION=viewevent&SYS.ID=017-001-000")
				.go();
	}

	@Override public String getHomeLink() {
		String queryParams = "";
		if (TextUtils.isEmpty(request.businessId)) {
			queryParams += TextUtils.isEmpty(schedule.url)
					? "?id=" + request.businessId
					: "?activeid=" + request.businessId;
		}

		if (request.pageId != -1) {
			queryParams += TextUtils.isEmpty(queryParams)
					? "?pageId=1"
					: "&pageId=1";
		}

		if (TextUtils.isEmpty(schedule.url)) {
			String url = "file:///android_asset/wechat/html/schedule/schedule.html";
			if (!TextUtils.isEmpty(request.businessId)) {
				queryParams = "#pageother" + queryParams;
			}
			return url + queryParams;
		}
		return verifyRemoteURL(schedule.url + queryParams);
	}

	private boolean isOldestVersion() {
		return !FunctionManager.isNative(Func.Schedule) && TextUtils.isEmpty(schedule.url);
	}
}
