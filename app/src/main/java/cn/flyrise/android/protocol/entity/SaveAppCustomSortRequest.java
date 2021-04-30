package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author 社会主义接班人
 * @since 2018-07-27 18:45
 */
public class SaveAppCustomSortRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "AppsRequest";
	}

	public String method;
	public String customIds;
	public String quickIds;

}
