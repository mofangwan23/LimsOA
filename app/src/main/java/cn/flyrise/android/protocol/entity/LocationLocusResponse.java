package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.location.bean.LocationLists;
import cn.flyrise.feep.location.bean.LocationWorkingTimes;
import cn.flyrise.feep.location.bean.LocusDates;
import cn.flyrise.feep.location.bean.LocusPersonLists;
import java.util.List;

/**
 * 类描述：考勤轨迹的响应类
 * @author 罗展健
 * @version 1.0
 */
public class LocationLocusResponse extends ResponseContent {

	private String userId;
	private String userName;
	private String phone;
	private String requestType;

	public List<String> departmentList;         // 主分管的部门
	public List<String> departmentList2;         // 子分管的部门
	private List<LocusPersonLists> personList;   // 人员列表
	private List<LocusDates> dateList;     // 时间列表
	private List<LocationLists> locationList; // 考勤轨迹列表
	private List<LocationWorkingTimes> workingTimes; // 上下班时间列表

	private String isoutsign; //Y则允许超范围打卡签到，不存在字段或值不为Y则不允许超范围签到

	private String pname;       //考勤名称
	private String paddress;   //考勤地址
	private String longitude; //经度
	private String latitude;  //纬度
	private String range;     //考勤范围

	public String getPname() {
		return pname;
	}

	public void setPname(String pname) {
		this.pname = pname;
	}

	public String getPaddress() {
		return paddress;
	}

	public void setPaddress(String paddress) {
		this.paddress = paddress;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public String getIsoutsign() {
		return isoutsign;
	}

	public void setIsoutsign(String isoutsign) {
		this.isoutsign = isoutsign;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public List<LocusPersonLists> getPersonList() {
		return personList;
	}

	public void setPersonList(List<LocusPersonLists> personList) {
		this.personList = personList;
	}

	public List<LocusDates> getDateList() {
		return dateList;
	}

	public void setDateList(List<LocusDates> dateList) {
		this.dateList = dateList;
	}

	public List<LocationLists> getLocationList() {
		return locationList;
	}

	public void setLocationList(List<LocationLists> locationList) {
		this.locationList = locationList;
	}

	public List<LocationWorkingTimes> getWorkingTimes() {
		return workingTimes;
	}

	public void setWorkingTimes(List<LocationWorkingTimes> workingTimes) {
		this.workingTimes = workingTimes;
	}

}
