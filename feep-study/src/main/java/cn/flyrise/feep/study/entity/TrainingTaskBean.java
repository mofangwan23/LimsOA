package cn.flyrise.feep.study.entity;

public class TrainingTaskBean {

	/**
	 * {"TASKNAME":"5555555","TASKNO":"PXR20190009"}
	 */

	private String TASKNAME;
	private String TASKNO;
	private String TASKID;

	public String getTASKNAME() {
		return TASKNAME;
	}

	public void setTASKNAME(String TASKNAME) {
		this.TASKNAME = TASKNAME;
	}

	public String getTASKNO() {
		return TASKNO;
	}

	public String getTASKID() {
		return TASKID;
	}

	public void setTASKNO(String TASKNO) {
		this.TASKNO = TASKNO;
	}
}
