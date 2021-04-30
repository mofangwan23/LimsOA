/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-5 上午9:31:36
 */

package cn.flyrise.feep.commonality.bean;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.io.Serializable;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</   br> 修改时间：2013-3-5</br> 修改备注：</br>
 */
public class FEListItem implements Serializable {

	private static final long serialVersionUID = -6655246809693820803L;
	private String title;
	private String id;
	private String sendTime;
	private String sendUser;
	private String sendUserId;

	public String getSendUserId() {
		return sendUserId;
	}

	public void setSendUserId(String sendUserId) {
		this.sendUserId = sendUserId;
	}

	private String searchKey;
	private String sendUserImg;

	public String getSendUserImg() {
		return sendUserImg;
	}

	public void setSendUserImg(String sendUserImg) {
		this.sendUserImg = sendUserImg;
	}

	// 为位置上报历史记录增加的字段2013-10-22
	private String date;
	private String whatDay;
	private String time;
	private String address;
	private String name;
	// 新增位置上报的现场图片2015-2-6
	private String imageHerf;
	// 强制拍照的描述内容
	private String pdesc;
	// 图片的缩略图
	private String sguid;
	private String guid;
	private String content;
	private String badge;
	private String category;
	private boolean isNews;

	//知识中心文件ID
	private String businessID;

	//急件程度
	private String important;

	//针对某个公司的要求，给任务标题标位红色。 //#5309
	private String level;

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getBadge() {
		return badge;
	}

	public void setBadge(String badge) {
		this.badge = badge;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private boolean isOneDay;

	public boolean isOneDay() {
		return isOneDay;
	}

	public void setOneDay() {
		this.isOneDay = true;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getWhatDay() {
		return whatDay;
	}

	public void setWhatDay(String whatDay) {
		this.whatDay = whatDay;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPdesc() {
		return pdesc;
	}

	public void setPdesc(String pdesc) {
		this.pdesc = pdesc;
	}

	public String getSguid() {
		return sguid;
	}

	public void setSguid(String sguid) {
		this.sguid = sguid;
	}

	// 为通知列表增加的字段
	private String msgId;
	private String msgType;
	private String requestType;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getSendUser() {
		return sendUser;
	}

	public void setSendUser(String sendUser) {
		this.sendUser = sendUser;
	}

	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public int getRequestType() {
		return CommonUtil.parseInt(requestType);
	}

	public void setRequestType(String requestType) {
		if ("9".equals(requestType)) { // 模块号转换为列表号，会议和报表需要转换
			requestType = "7";
		}
		else if ("13".equals(requestType)) {
			requestType = "8";
		}
		this.requestType = requestType;
	}

	public String getImageHerf() {
		return imageHerf;
	}

	public void setImageHerf(String imageHerf) {
		this.imageHerf = imageHerf;
	}

	public String getBusinessID() {
		return businessID;
	}

	public void setBusinessID(String businessID) {
		this.businessID = businessID;
	}

	public void setNews(boolean isNews) {
		this.isNews = isNews;
	}

	public boolean isNews() {
		return this.isNews;
	}

	public String getImportant() {
		return important;
	}

	public void setImportant(String important) {
		this.important = important;
	}
}
