package cn.flyrise.feep.study.entity;

import cn.flyrise.feep.core.network.request.ResponseContent;

public class TrainsignResponse extends ResponseContent {


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
