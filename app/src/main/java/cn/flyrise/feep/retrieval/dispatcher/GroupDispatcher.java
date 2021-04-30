package cn.flyrise.feep.retrieval.dispatcher;

import android.content.Context;
import cn.flyrise.feep.retrieval.bean.GroupRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.squirtlez.frouter.FRouter;

/**
 * @author ZYP
 * @since 2018-05-08 18:20
 */
public class GroupDispatcher implements RetrievalDispatcher {

	@Override public void jumpToSearchPage(Context context, String keyword) {
		FRouter.build(context, "/im/chat/search/group")
				.withString("keyword", keyword)
				.go();
	}

	@Override public void jumpToDetailPage(Context context, Retrieval retrieval) {
		GroupRetrieval group = (GroupRetrieval) retrieval;
		FRouter.build(context, "/im/chat")
				.withString("Extra_chatID", group.conversationId)
				.withInt("Extra_chatType", 0X104)
				.go();
	}
}
