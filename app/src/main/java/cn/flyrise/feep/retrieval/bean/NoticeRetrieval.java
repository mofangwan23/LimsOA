package cn.flyrise.feep.retrieval.bean;

import cn.flyrise.feep.core.common.X.RequestType;

/**
 * @author ZYP
 * @since 2018-05-09 16:20
 */
public class NoticeRetrieval extends BusinessRetrieval {

	public int getRequestType() {
		return RequestType.Announcement;
	}

}
