package cn.flyrise.feep.study.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class TrainingSignRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "TrainingSignRequest";
	}

	private String requestType = "1";
	private String recordId = "";
	private String type = "1";
	private String sdate = "";
	private String edate= "";
	private String trainTaskId= "";
	private String score= "";
	private String ispass = "";
	private String signDate = "";
	private String INFOID;

	private String pId;
	private String uId;

	private String speed;//学习时长
	private String personTaskId;//个人任务id
	private String dataId;//附件id

	/**
	 * 电子签名验证
	 */
	private String wf_inforid;
	private String userId;
	private String password;
	private String remark;

	//完成培训/考试
	private String serverId;
	private String master_key;
	private String tableName;

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public void setMaster_key(String master_key) {
		this.master_key = master_key;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setWf_inforid(String wf_inforid) {
		this.wf_inforid = wf_inforid;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void setAction(String action) {
		this.action = action;
	}

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

	public void setPersonTaskId(String personTaskId) {
		this.personTaskId = personTaskId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	private String dataType;//附件类型

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public void setpId(String pId) {
		this.pId = pId;
	}

	public void setuId(String uId) {
		this.uId = uId;
	}

	public void setSignDate(String signDate) {
		this.signDate = signDate;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setSdate(String sdate) {
		this.sdate = sdate;
	}

	public void setEdate(String edate) {
		this.edate = edate;
	}

	public void setTrainTaskId(String trainTaskId) {
		this.trainTaskId = trainTaskId;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public void setIspass(String ispass) {
		this.ispass = ispass;
	}

	public String getINFOID() {
		return INFOID;
	}

	public void setINFOID(String INFOID) {
		this.INFOID = INFOID;
	}
}
