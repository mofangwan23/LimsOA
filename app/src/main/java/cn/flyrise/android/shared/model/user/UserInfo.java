//
// UserInfo.java
// feep
//
// Created by LuTH on 2011-11-30.
// Copyright 2011 flyrise. All rights reserved.
//
package cn.flyrise.android.shared.model.user;

import android.text.TextUtils;
import cn.flyrise.android.protocol.model.MainMenu;
import java.io.Serializable;
import java.util.List;

public class UserInfo implements Serializable {

	private static final long serialVersionUID = -7060210544600464481L;

	private String loginName;                               // 登录名
	private String userName;                                // 中文姓名
	private String userID;
	private String password;
	private boolean isSavePassword;
	private boolean isAutoLogin;
	private boolean isHttps;
	private String serverAddress;
	private String serverPort;
	private String serverHttpsPort;
	private String department;

	private String imid;
	private String userPost;
	private String avatarUrl;
	private boolean isVpn;

	private List<MainMenu> bottomMenu;

	public List<MainMenu> getBottomMenu() {
		return bottomMenu;
	}

	public void setBottomMenu(List<MainMenu> bottomMenu) {
		this.bottomMenu = bottomMenu;
	}

	public boolean isVpn() {
		return isVpn;
	}

	public void setVpn(boolean vpn) {
		isVpn = vpn;
	}

	public String getUserPost() {
		return userPost;
	}

	public void setUserPost(String userPost) {
		this.userPost = userPost;
	}

	public void setAvatarUrl(String avatarUrl) {
		if (!TextUtils.isEmpty(avatarUrl)) {
			this.avatarUrl = avatarUrl.substring(0, avatarUrl.lastIndexOf(".png") + 4);
		}
	}

	public String getAvatarUrl() {
		return this.avatarUrl;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	/**
	 * GET/SET
	 */

	public String getUserName() {
		return userName;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSavePassword() {
		return isSavePassword;
	}

	public void setSavePassword(boolean isSavePassword) {
		this.isSavePassword = isSavePassword;
	}

	public boolean isAutoLogin() {
		return isAutoLogin;
	}

	public void setAutoLogin(boolean isAutoLogin) {
		this.isAutoLogin = isAutoLogin;
	}

	public boolean isHttps() {
		return isHttps;
	}

	public void setHttps(boolean isHttps) {
		this.isHttps = isHttps;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public String getUrl() {
		final String isHttpsStr = this.isHttps ? "https://" : "http://";
//        return isHttpsStr + this.getServerAddress() + ":" + this.getServerPort();
		return isHttpsStr + this.getServerAddress() + (TextUtils.isEmpty(this.getServerPort()) ? "" : ":" + this.getServerPort());
	}

	public String getServerHttpsPort() {
		return serverHttpsPort;
	}

	public void setServerHttpsPort(String serverHttpsPort) {
		this.serverHttpsPort = serverHttpsPort;
	}

	public String getImid() {
		return imid;
	}

	public void setImid(String imid) {
		this.imid = imid;
	}

}
