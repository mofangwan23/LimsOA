package cn.flyrise.feep.retrieval.repository;

import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_APPROVAL;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_CHAT;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_CONTACT;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_FILES;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_GROUP;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_MEETING;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_NEWS;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_NOTICE;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_PLAN;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_SCHEDULE;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_ALL_MESSAGE;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import rx.Subscriber;

/**
 * @author ZYP
 * @since 2018-05-09 10:57
 * 检索仓库，每个仓库只处理一种数据
 * 比如 ContactRetrievalRepository：只处理联系人的查找。符合单一智能原则
 */
public abstract class RetrievalRepository {

	protected String mKeyword;

	public Context mContext;

	/**
	 * 根据关键字检索相关数据信息
	 * @param keyword 关键字
	 */
	public abstract void search(Subscriber<? super RetrievalResults> subscriber, String keyword);

	/**
	 * 获取当前检索类型
	 */
	protected abstract int getType();

	/**
	 * 创建一个空数据对象
	 */
	protected abstract Retrieval newRetrieval();

	protected Retrieval header(String content) {
		return newTag(Retrieval.VIEW_TYPE_HEADER, content);
	}

	protected Retrieval footer(String content) {
		return newTag(Retrieval.VIEW_TYPE_FOOTER, content);
	}

	private Retrieval newTag(int viewType, String content) {
		Retrieval retrieval = newRetrieval();
		retrieval.retrievalType = getType();
		retrieval.viewType = viewType;
		retrieval.content = content;
		return retrieval;
	}

	protected RetrievalResults emptyResult() {
		return new RetrievalResults.Builder()
				.retrievalType(getType())
				.create();
	}

	/**
	 * 关键字高亮
	 */
	protected String fontDeepen(String content, String keyword) {
		if (TextUtils.isEmpty(content)) return content;
		if (!content.contains(keyword)) return content;

		String deepen = "<font color=\"#28B9FF\">" + keyword + "</font>";
		return content.replace(keyword, deepen);
	}

	/**
	 * 静态工厂方法，用于创建相关数据仓库
	 */
	public static RetrievalRepository newRepository(int retrievalType) {
		if (retrievalType == TYPE_CONTACT) return new ContactRetrievalRepository();
		if (retrievalType == TYPE_GROUP) return new GroupRetrievalRepository();
		if (retrievalType == TYPE_CHAT) return new ChatRetrievalRepository();
		if (retrievalType == TYPE_NEWS) return new NewsRetrievalRepository();
		if (retrievalType == TYPE_FILES) return new FileRetrievalRepository();
		if (retrievalType == TYPE_APPROVAL) return new ApprovalRetrievalRepository();
		if (retrievalType == TYPE_SCHEDULE) return new ScheduleRetrievalRepository();
		if (retrievalType == TYPE_PLAN) return new PlanRetrievalRepository();
		if (retrievalType == TYPE_NOTICE) return new NoticeRetrievalRepository();
		if (retrievalType == TYPE_MEETING) return new MeetingRetrievalRepository();
		if (retrievalType == TYPE_ALL_MESSAGE) return new AllRetrievalRepository();
		return null;
	}

}
