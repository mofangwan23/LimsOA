package cn.flyrise.feep.schedule.model;


import android.support.annotation.Keep;
import android.text.TextUtils;

/**
 * Created by klc on 2018/3/26.
 */
@Keep
public class ScheduleReply {

	private String replyId;
	private String userId;
	private String replyContent;
	private String userName;
	private String lastUpdateTime;
	private String replyTime;


	public String getReplyId() {
		return replyId;
	}

	public String getUserId() {
		return userId;
	}

	public String getReplyContent() {
		return replyContent;
	}

	public String getUserName() {
		return userName;
	}

	public String getTime() {
		if (TextUtils.isEmpty(lastUpdateTime)) {
			return replyTime;
		}
		else {
			return lastUpdateTime;
		}
	}

	public String getLastUpdateTime() {
		return lastUpdateTime;
	}

	public String getReplyTime() {
		return replyTime;
	}
}
