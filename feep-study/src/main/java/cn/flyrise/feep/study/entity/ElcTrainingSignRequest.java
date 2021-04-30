package cn.flyrise.feep.study.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class ElcTrainingSignRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "TrainingSignRequest";
	}

	private String wf_inforid;
	private String userId;
	private String password;
	private String remark;
	/**
	 * 送办  save_sent_doc
	 * 新增  new
	 * 确认  enter
	 * 修改  edit
	 * 删除  delete
	 * 提交  submit
	 * 签到  sign
	 * 退回 doc_back_save
	 */
	private String action;

	//13 电子签名
	private String type;

	private String requestType = "1";

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getWf_inforid() {
		return wf_inforid;
	}

	public void setWf_inforid(String wf_inforid) {
		this.wf_inforid = wf_inforid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
