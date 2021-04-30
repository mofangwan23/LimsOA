package cn.flyrise.feep.study.entity;

import cn.flyrise.feep.core.network.request.RequestContent;
import java.util.List;

public class QuestionSubmitRequest extends RequestContent {

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
	private String uId;
	private String pId;
	private List<String> qId;
	private List<String> qType;
	private List<String> user_answer;
	private List<String> status; //1是正确，0是错误
	private List<String> scores;

	public void setScores(List<String> scores) {
		this.scores = scores;
	}

	public void setStatus(List<String> status) {
		this.status = status;
	}

	public void setuId(String uId) {
		this.uId = uId;
	}

	public void setpId(String pId) {
		this.pId = pId;
	}

	public void setqId(List<String> qId) {
		this.qId = qId;
	}

	public void setqType(List<String> qType) {
		this.qType = qType;
	}

	public void setUser_answer(List<String> user_answer) {
		this.user_answer = user_answer;
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
}
