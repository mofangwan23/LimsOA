package cn.flyrise.feep.study.entity;

import android.os.Parcel;
import android.os.Parcelable;
import cn.flyrise.feep.core.network.request.ResponseContent;
import java.io.Serializable;
import java.util.List;

public class GetQuestionResponse extends ResponseContent implements Serializable {


	private List<InfoBean> info;
	private List<DatalistBean> datalist;

	public List<InfoBean> getInfo() { return info;}

	public void setInfo(List<InfoBean> info) { this.info = info;}

	public List<DatalistBean> getDatalist() { return datalist;}

	public void setDatalist(List<DatalistBean> datalist) { this.datalist = datalist;}

	public static class InfoBean implements Serializable {

		/**
		 * paper_name : 测试
		 * paper_minute : 60
		 * total_score : 80
		 * pass_score : 31
		 */

		private String paper_name;
		private String paper_minute;
		private String total_score;
		private String pass_score;
		private String serverId;
		private String master_key;
		private String tableName;

		public String getServerId() {
			return serverId;
		}

		public String getMaster_key() {
			return master_key;
		}

		public String getTableName() {
			return tableName;
		}

		public String getPaper_name() { return paper_name;}

		public void setPaper_name(String paper_name) { this.paper_name = paper_name;}

		public String getPaper_minute() { return paper_minute;}

		public void setPaper_minute(String paper_minute) { this.paper_minute = paper_minute;}

		public String getTotal_score() { return total_score;}

		public void setTotal_score(String total_score) { this.total_score = total_score;}

		public String getPass_score() { return pass_score;}

		public void setPass_score(String pass_score) { this.pass_score = pass_score;}
	}

	public static class DatalistBean implements Serializable {

		/**
		 * POSTDATE : 10 30 2019 10:12AM
		 * SCORE : 10
		 * DBID : 3
		 * KEYDESC :
		 * SKEY : B
		 * QFROM : 1
		 * STATUS : 1
		 * ADMINID : 11
		 * QTYPE : 1
		 * ID : 10
		 * QLEVEL : 5
		 * CONTENT : 证明任何操作规程（或方法）、生产工艺或系统能达到预期的结果，这一系列的活动通常称之为：（&nbsp;&nbsp; ） <br />
		 <br />
		 * DB : [{"SALISA":"A","SOPTION":"检验","QID":"10"},{"SALISA":"B","SOPTION":"验证","QID":"10"},{"SALISA":"C","SOPTION":"工艺考核","QID":"10"},{"SALISA":"D","SOPTION":"质量保证","QID":"10"}]
		 */

		private String POSTDATE;
		private String SCORE;
		private String DBID;
		private String KEYDESC;
		private String SKEY;
		private String QFROM;
		private String STATUS;
		private String ADMINID;
		private String QTYPE;
		private String ID;
		private String QLEVEL;
		private String CONTENT;
		private List<DBBean> DB;
		private String userAnswer;
		private String USER_ANSWER;
		private String RW;

		public String getRW() {
			return RW;
		}

		public String getUSER_ANSWER() {
			return USER_ANSWER;
		}

		public String getUserAnswer() {
			return userAnswer;
		}

		public void setUserAnswer(String userAnswer) {
			this.userAnswer = userAnswer;
		}

		public String getPOSTDATE() { return POSTDATE;}

		public void setPOSTDATE(String POSTDATE) { this.POSTDATE = POSTDATE;}

		public String getSCORE() { return SCORE;}

		public void setSCORE(String SCORE) { this.SCORE = SCORE;}

		public String getDBID() { return DBID;}

		public void setDBID(String DBID) { this.DBID = DBID;}

		public String getKEYDESC() { return KEYDESC;}

		public void setKEYDESC(String KEYDESC) { this.KEYDESC = KEYDESC;}

		public String getSKEY() { return SKEY;}

		public void setSKEY(String SKEY) { this.SKEY = SKEY;}

		public String getQFROM() { return QFROM;}

		public void setQFROM(String QFROM) { this.QFROM = QFROM;}

		public String getSTATUS() { return STATUS;}

		public void setSTATUS(String STATUS) { this.STATUS = STATUS;}

		public String getADMINID() { return ADMINID;}

		public void setADMINID(String ADMINID) { this.ADMINID = ADMINID;}

		public String getQTYPE() { return QTYPE;}

		public void setQTYPE(String QTYPE) { this.QTYPE = QTYPE;}

		public String getID() { return ID;}

		public void setID(String ID) { this.ID = ID;}

		public String getQLEVEL() { return QLEVEL;}

		public void setQLEVEL(String QLEVEL) { this.QLEVEL = QLEVEL;}

		public String getCONTENT() { return CONTENT;}

		public void setCONTENT(String CONTENT) { this.CONTENT = CONTENT;}

		public List<DBBean> getDB() { return DB;}

		public void setDB(List<DBBean> DB) { this.DB = DB;}

		public static class DBBean implements Serializable{

			public DBBean(String SALISA) {
				this.SALISA = SALISA;
			}

			/**
			 * SALISA : A
			 * SOPTION : 检验
			 * QID : 10
			 */

			private String SALISA;
			private String SOPTION;
			private String QID;

			public String getSALISA() { return SALISA;}

			public void setSALISA(String SALISA) { this.SALISA = SALISA;}

			public String getSOPTION() { return SOPTION;}

			public void setSOPTION(String SOPTION) { this.SOPTION = SOPTION;}

			public String getQID() { return QID;}

			public void setQID(String QID) { this.QID = QID;}
		}
	}
}
