package cn.flyrise.feep.retrieval.vo;

import android.support.annotation.Keep;
import android.text.TextUtils;

/**
 * @author ZYP
 * @since 2018-05-07 14:49
 *
 * 检索类型，目前服务端支持这么多种搜索，其中联系人、聊天、群聊的搜索在本地
 * 1001：审批
 * 1002：文件
 * 1003：日程
 * 1004：计划
 * 1005：联系人
 * 1006：聊天记录
 * 1007：新闻
 * 1008：公告
 * 1009：会议
 */
@Keep
public final class RetrievalType {

	public static final int TYPE_CONTACT = 1;   // 联系人
	public static final int TYPE_GROUP = 2;     // 群聊
	public static final int TYPE_CHAT = 3;      // 聊天记录
	public static final int TYPE_NEWS = 4;      // 新闻
	public static final int TYPE_FILES = 5;     // 文件
	public static final int TYPE_APPROVAL = 6;  // 审批
	public static final int TYPE_SCHEDULE = 7;  // 日程
	public static final int TYPE_PLAN = 8;      // 计划
	public static final int TYPE_NOTICE = 9;    // 公告
	public static final int TYPE_MEETING = 10;  // 会议
	public static final int TYPE_ALL_MESSAGE = 101;  // 搜索全部

	public String key;
	public String value;

	public RetrievalType(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public int getRetrievalType() {
		if (TextUtils.equals(key, "1001")) return TYPE_APPROVAL;
		if (TextUtils.equals(key, "1002")) return TYPE_FILES;
		if (TextUtils.equals(key, "1003")) return TYPE_SCHEDULE;
		if (TextUtils.equals(key, "1004")) return TYPE_PLAN;
		if (TextUtils.equals(key, "1005")) return TYPE_CONTACT;
		if (TextUtils.equals(key, "1006")) return TYPE_CHAT;
		if (TextUtils.equals(key, "1007")) return TYPE_NEWS;
		if (TextUtils.equals(key, "1008")) return TYPE_NOTICE;
		if (TextUtils.equals(key, "1009")) return TYPE_MEETING;
		if (TextUtils.equals(key, "2010")) return TYPE_ALL_MESSAGE;
		return -1;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RetrievalType)) return false;

		RetrievalType that = (RetrievalType) o;

		if (!key.equals(that.key)) return false;
		return value.equals(that.value);
	}

	@Override public int hashCode() {
		int result = key.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}
}
