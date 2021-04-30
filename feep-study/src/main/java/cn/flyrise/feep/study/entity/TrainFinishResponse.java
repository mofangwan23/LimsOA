package cn.flyrise.feep.study.entity;

import cn.flyrise.feep.core.network.request.ResponseContent;

public class TrainFinishResponse extends ResponseContent {


	/**
	 * result : {"code":0,"mes":"签到失败，已超过培训任务结束时间！"}
	 */

	private ResultBean data;

	public ResultBean getResult() { return data;}

	public void setResult(ResultBean result) { this.data = result;}

	public static class ResultBean {

		/**
		 * code : 0
		 * mes : 签到失败，已超过培训任务结束时间！
		 */

		private int code;
		private String mes;

		public int getCode() { return code;}

		public void setCode(int code) { this.code = code;}

		public String getMes() { return mes;}

		public void setMes(String mes) { this.mes = mes;}
	}
}
