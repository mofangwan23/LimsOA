package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * 新建：陈冕;
 * 日期： 2018-6-26-11:23.
 */
public class AppsRequest extends RequestContent {

	public String method = "appMsgToken";

	public int channel = 0;//0极光、1华为、2小米

	public String token;

	@Override
	public String getNameSpace() {
		return "AppsRequest";
	}
}
