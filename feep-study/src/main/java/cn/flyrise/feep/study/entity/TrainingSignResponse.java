package cn.flyrise.feep.study.entity;

import cn.flyrise.feep.core.network.request.ResponseContent;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class TrainingSignResponse extends ResponseContent implements Serializable{

	private List<QueryBean> data;

	public List<QueryBean> getData() { return data;}

	public static class QueryBean implements Serializable {

		private String ID;
		private String PAPER_NAME;

		private String trainTask_ID;

		private String SUBMITDATE;
		private String PAPERID;
		private String ISPASS;
		private String INFOID;

		public String getINFOID() {
			return INFOID;
		}

		public String getSUBMITDATE() {
			return SUBMITDATE;
		}

		public String getPAPERID() {
			return PAPERID;
		}

		public String getISPASS() {
			return ISPASS;
		}

		public String getTrainTask_ID() {
			return trainTask_ID;
		}

		public String getID() {
			return ID;
		}

		public String getPAPER_NAME() {
			return PAPER_NAME;
		}
	}

}
