//
// UserBean.java
// feep
//
// Created by LuTH on 2011-12-20.
// Copyright 2011 flyrise. All rights reserved.
//
package cn.flyrise.android.shared.bean;

import java.io.Serializable;

/**
 * 用户对象类
 *
 * @author LuTH
 */
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String _id;
    private String userID;
    private String loginName;            // 登录名
    private String userName;             // 中文姓名
    private String password;
    private String token;
    private boolean isSavePassword;
    private boolean isAutoLogin;
    private boolean isHttps;
    private String serverAddress;
    private String serverPort;
    private String time;
    private String httpsPort;

    public UserBean () {

    }

    // ************************* VPN 相关字段 ****************************** /
    private boolean isVPN;
    private String vpnAddress;
    private String vpnPort;
    private String vpnUsername;
    private String vpnPassword;

    public boolean isVPN() {
        return isVPN;
    }

    public void setVPN(boolean VPN) {
        isVPN = VPN;
    }

    public String getVpnAddress() {
        return vpnAddress;
    }

    public void setVpnAddress(String vpnAddress) {
        this.vpnAddress = vpnAddress;
    }

    public String getVpnPort() {
        return vpnPort;
    }

    public void setVpnPort(String vpnPort) {
        this.vpnPort = vpnPort;
    }

    public String getVpnUsername() {
        return vpnUsername;
    }

    public void setVpnUsername(String vpnUsername) {
        this.vpnUsername = vpnUsername;
    }

    public String getVpnPassword() {
        return vpnPassword;
    }

    public void setVpnPassword(String vpnPassword) {
        this.vpnPassword = vpnPassword;
    }

    public String get_id () {
        return _id;
    }

    public void set_id (String _id) {
        this._id = _id;
    }

    public String getUserID () {
        return userID;
    }

    public void setUserID (String userID) {
        this.userID = userID;
    }

    public String getLoginName () {
        return loginName;
    }

    public void setLoginName (String loginName) {
        this.loginName = loginName;
    }

    public String getUserName () {
        return userName;
    }

    public void setUserName (String userName) {
        this.userName = userName;
    }

    public String getPassword () {
        return password;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public String getToken () {
        return token;
    }

    public void setToken (String token) {
        this.token = token;
    }

    public boolean isSavePassword () {
        return isSavePassword;
    }

    public void setSavePassword (boolean isSavePassword) {
        this.isSavePassword = isSavePassword;
    }

    public boolean isAutoLogin () {
        return isAutoLogin;
    }

    public void setAutoLogin (boolean isAutoLogin) {
        this.isAutoLogin = isAutoLogin;
    }

    public boolean isHttps () {
        return isHttps;
    }

    public void setHttps (boolean isHttps) {
        this.isHttps = isHttps;
    }

    public String getServerAddress () {
        return serverAddress;
    }

    public void setServerAddress (String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getServerPort () {
        return serverPort;
    }

    public void setServerPort (String serverPort) {
        this.serverPort = serverPort;
    }

    public String getTime () {
        return time;
    }

    public void setTime (String time) {
        this.time = time;
    }

    public String getHttpsPort () {
        return httpsPort;
    }

    public void setHttpsPort (String httpsPort) {
        this.httpsPort = httpsPort;
    }

}
