package cn.flyrise.feep.retrieval.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;
import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-09 17:32
 */
public class RetrievalSearchRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "SearchRequest";
	}

	/**
	 * 审批：searchTodo
	 * 文件：searchFile
	 * 日程：searchAgenda
	 * 计划：searchWorkPlan
	 * 新闻：searchNews
	 * 公告：searchNotice
	 * 会议：searchMeeting
	 * 全部：getHomeIndex
	 */
	public String method;
	@SerializedName("keyWord") public String keyword;

	public RetrievalSearchRequest() { }

	private RetrievalSearchRequest(String method, String keyword) {
		this.method = method;
		this.keyword = keyword;
	}

	public static RetrievalSearchRequest searchAll(String keyword) {
		return new RetrievalSearchRequest("getHomeIndex", keyword);
	}

	public static RetrievalSearchRequest searchTodo(String keyword) {
		return new RetrievalSearchRequest("searchTodo", keyword);
	}

	public static RetrievalSearchRequest searchFile(String keyword) {
		return new RetrievalSearchRequest("searchFile", keyword);
	}

	public static RetrievalSearchRequest searchAgenda(String keyword) {
		return new RetrievalSearchRequest("searchAgenda", keyword);
	}

	public static RetrievalSearchRequest searchWorkPlan(String keyword) {
		return new RetrievalSearchRequest("searchWorkPlan", keyword);
	}

	public static RetrievalSearchRequest searchNews(String keyword) {
		return new RetrievalSearchRequest("searchNews", keyword);
	}

	public static RetrievalSearchRequest searchNotice(String keyword) {
		return new RetrievalSearchRequest("searchNotice", keyword);
	}

	public static RetrievalSearchRequest searchMeeting(String keyword) {
		return new RetrievalSearchRequest("searchMeeting", keyword);
	}

}
