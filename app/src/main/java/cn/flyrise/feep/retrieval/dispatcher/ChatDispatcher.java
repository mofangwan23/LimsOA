package cn.flyrise.feep.retrieval.dispatcher;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.retrieval.bean.ChatRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.squirtlez.frouter.FRouter;
import cn.squirtlez.frouter.RouteCreator;
import com.hyphenate.easeui.busevent.ChatContent;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-04 15:18
 */
public class ChatDispatcher implements RetrievalDispatcher {

	@Override public void jumpToSearchPage(Context context, String keyword) {
		FRouter.build(context, "/im/chat/search/more")
				.withString("keyword", keyword)
				.go();
	}

	@Override public void jumpToDetailPage(Context context, Retrieval retrieval) {
		ChatRetrieval chatRetrieval = (ChatRetrieval) retrieval;
		if (TextUtils.isEmpty(chatRetrieval.messageId)) {
			FRouter.build(context, "/im/chat/search")
					.withString("conversationId", chatRetrieval.conversationId)
					.withString("keyword", chatRetrieval.keyword)
					.go();
			return;
		}

		// 单条消息，直接滚过去
		RouteCreator routeCreator = FRouter.build(context, "/im/chat").withString("Extra_chatID", chatRetrieval.conversationId);
		if (chatRetrieval.isGroup) {
			routeCreator.withInt("Extra_chatType", 0X104);
		}

		routeCreator.go();

		Observable.timer(500, TimeUnit.MILLISECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(time -> {
					EventBus.getDefault().post(new ChatContent.SeekToMsgEvent(chatRetrieval.messageId));
				});
	}
}
