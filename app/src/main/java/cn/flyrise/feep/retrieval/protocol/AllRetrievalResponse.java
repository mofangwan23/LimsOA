package cn.flyrise.feep.retrieval.protocol;

import cn.flyrise.android.protocol.entity.AddressBookResponse;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Create by cm132 on 2018/12/22.
 * Describe:获取全文检索所有消息
 */
public class AllRetrievalResponse extends ResponseContent {

	public Retrieval data;

	public class Retrieval {

		public ApprovalRetrievalResponse.SearchResult S1001;
		public FileRetrievalResponse.SearchResult S1002;
		public ScheduleRetrievalResponse.SearchResult S1003;
		public PlanRetrievalResponse.SearchResult S1004;
		public NewsRetrievalResponse.SearchResult S1007;
		public NoticeRetrievalResponse.SearchResult S1008;
		public MeetingRetrievalResponse.SearchResult S1009;
	}

}
