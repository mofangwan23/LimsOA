package cn.flyrise.feep.x5;

import android.content.Intent;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.x5.dispatcher.ActivityDispatcher;
import cn.flyrise.feep.x5.dispatcher.CRMDispatcher;
import cn.flyrise.feep.x5.dispatcher.DoduoDispatcher;
import cn.flyrise.feep.x5.dispatcher.HeadLineDispatcher;
import cn.flyrise.feep.x5.dispatcher.KnowledgeDispatcher;
import cn.flyrise.feep.x5.dispatcher.RequestDispatcher;
import cn.flyrise.feep.x5.dispatcher.ScheduleDispatcher;
import cn.flyrise.feep.x5.dispatcher.UnknownDispatcher;
import cn.flyrise.feep.x5.dispatcher.VoteDispatcher;

/**
 * @author 社会主义接班人
 * @since 2018-09-17 16:15
 */
public class URLTransferStation {

	private URLTransferStation() { }

	private static class Singleton {

		private static final URLTransferStation sInstance = new URLTransferStation();
	}

	public static URLTransferStation getInstance() {
		return Singleton.sInstance;
	}

	public RequestDispatcher getDispatcher(Intent intent) {
		return produceDispatcher(new Request.Builder()
				.messageId(intent.getStringExtra("messageId"))
				.businessId(intent.getStringExtra("businessId"))
				.appointURL(intent.getStringExtra("appointURL"))
				.moduleId(intent.getIntExtra("moduleId", Func.Default))
				.pageId(intent.getIntExtra("pageId", -1))
				.extra(intent.getStringExtra("extra"))
				.userIds(intent.getStringArrayListExtra("userIds"))
				.create());
	}

	private RequestDispatcher produceDispatcher(Request request) {
		RequestDispatcher dispatcher;
		switch (request.moduleId) {
			case Func.Knowledge:
				dispatcher = new KnowledgeDispatcher(request);
				break;
			case Func.Schedule:
				dispatcher = new ScheduleDispatcher(request);
				break;
			case Func.Vote:
				dispatcher = new VoteDispatcher(request);
				break;
			case Func.Activity:
				dispatcher = new ActivityDispatcher(request);
				break;
			case Func.CRM:
				dispatcher = new CRMDispatcher(request);
				break;
			case Func.Headline:
				dispatcher = new HeadLineDispatcher(request);
				break;
			case Func.Dudu:
				dispatcher = new DoduoDispatcher(request);
				break;
			default:
				dispatcher = new UnknownDispatcher(request);
		}
		return dispatcher;
	}

}
