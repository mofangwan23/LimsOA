package cn.flyrise.feep.core.services.model;

import android.text.TextUtils;

/**
 * @author ZYP
 * @since 2017-02-08 10:10
 * 子模块能访问到的当前服务器信息
 */
public class NetworkInfo {

	public String serverAddress;
	public String serverPort;
	public boolean isHttps;

	public String buildServerURL() {
//        StringBuilder urlBuilder = new StringBuilder();
//        urlBuilder.append(isHttps ? "https" : "http")
//                .append("://")
//                .append(serverAddress).append(":")
//                .append(serverPort);
//        return urlBuilder.toString();
		StringBuilder urlBuilder = new StringBuilder(isHttps ? "https" : "http");
		urlBuilder.append("://").append(serverAddress);
		if (!TextUtils.isEmpty(serverPort)) urlBuilder.append(":").append(serverPort);
		return urlBuilder.toString();
	}

}
