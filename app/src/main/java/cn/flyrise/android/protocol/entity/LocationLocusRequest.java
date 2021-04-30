package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * 类描述：考勤轨迹的请求类
 * @author 罗展健
 * @version 1.0
 */
public class LocationLocusRequest extends RequestContent {

	public static final String NAMESPACE = "LocationLocusRequest";

	private String date;
	private String userId;
	private String requestType;
	private String brType;

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public void setBrType(String brType){
		this.brType = brType;
	}

	public String getBrType(){
		return brType;
	}

}
