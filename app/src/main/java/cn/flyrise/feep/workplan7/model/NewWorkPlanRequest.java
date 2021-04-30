package cn.flyrise.feep.workplan7.model;

import android.support.annotation.Keep;

import cn.flyrise.android.protocol.model.Flow;
import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author cm
 * @version 1.0 <br/>
 * 创建时间:  <br/>
 * 类说明 :
 */
@Keep
public class NewWorkPlanRequest extends RequestContent {

	public final String NAMESPACE = "WorkPlanRequest";
	protected String method;

	protected String title;
	protected String content;
	protected String userId;
	protected String startTime;
	protected String endTime;
	protected Flow receiveUsers;
	protected Flow CCUsers;
	protected Flow noticeUsers;
	protected String attachments;

	/**
	 * change by klc on 2018-5-7 11:04
	 * msg : Add
	 * workPlanId 给计划暂存 and 暂存计划发送使用的
	 */
	protected String id;

	/**
	 * change by klc on 2018-6-11 11:04
	 * msg : Add
	 * workPlanId 计划类型：1.日计划 2.周计划 3.月计划 4.其他计划
	 */
	protected String type;


	public void setMethod(String method) {
		this.method = method;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Flow getReceiveUsers() {
		return receiveUsers;
	}

	public void setReceiveUsers(Flow receiveUsers) {
		this.receiveUsers = receiveUsers;
	}

	public Flow getCCUsers() {
		return CCUsers;
	}

	public void setCCUsers(Flow cCUsers) {
		CCUsers = cCUsers;
	}

	public Flow getNoticeUsers() {
		return noticeUsers;
	}

	public void setNoticeUsers(Flow noticeUsers) {
		this.noticeUsers = noticeUsers;
	}

	public String getAttachments() {
		return attachments;
	}

	public void setAttachments(String attachments) {
		this.attachments = attachments;
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
