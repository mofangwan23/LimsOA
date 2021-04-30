//
// LoginResponse.java
// feep
//
// Created by LuTH on 2011-11-30.
// Copyright 2011 flyrise. All rights reserved.
//
package cn.flyrise.android.protocol.entity;

import android.text.TextUtils;
import cn.flyrise.android.protocol.model.MainMenu;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.mobilekey.model.MokeyInfo;
import java.util.List;

/**
 * 登录响应协议（通用）
 * @author LuTH
 */
public class LoginResponse extends ResponseContent {

	private String userID;
	private String userName;
	private String logoUrl;
	/**
	 * FE6.03增加 by罗展健
	 */
	private String accessToken;

	/**
	 * FE65增加服务器版本
	 */
	private String feVersion;

	/**
	 * FE65增加当前人头像
	 */
	private String headUrl;

	private String department;

	private String imid;

	private String imtoken;

	private String userPost;

	private List<MainMenu> bottomMenu;

	//联系人、知识中心新机制标志
	private String smallVersion;

	private boolean isGroupVersion;

	//手机盾的内容  2018/4/11 add by KLC
	private MokeyInfo mobileKeyMenu;

	public boolean isGroupVersion() {
		return isGroupVersion;
	}

	public void setGroupVersion(boolean groupVersion) {
		isGroupVersion = groupVersion;
	}

	public String getSmallVersion() {
		return smallVersion;
	}

	public void setSmallVersion(String smallVersion) {
		this.smallVersion = smallVersion;
	}

	public List<MainMenu> getBottomMenu() {
		return bottomMenu;
	}

	public void setBottomMenu(List<MainMenu> bottomMenu) {
		this.bottomMenu = bottomMenu;
	}

	public String getUserPost() {
		return userPost;
	}

	public void setUserPost(String userPost) {
		this.userPost = userPost;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public int getFeVersion() {
		if (TextUtils.isEmpty(feVersion)) return 0;
		int version = 0;
		try {
			version = Integer.valueOf(feVersion);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}

	public void setFeVersion(String feVersion) {
		this.feVersion = feVersion;
	}

	public String getHeadUrl() {
		return headUrl;
	}

	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getImid() {
		return imid;
	}

	public void setImid(String imid) {
		this.imid = imid;
	}

	public String getImtoken() {
		return imtoken;
	}

	public void setImtoken(String imtoken) {
		this.imtoken = imtoken;
	}

	public MokeyInfo getMobileKeyMenu() {
		return mobileKeyMenu;
	}
}
