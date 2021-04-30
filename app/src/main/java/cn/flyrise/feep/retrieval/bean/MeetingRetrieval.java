package cn.flyrise.feep.retrieval.bean;

import static cn.flyrise.feep.core.common.X.RequestType.Meeting;

/**
 * @author ZYP
 * @since 2018-05-09 16:20
 */
public class MeetingRetrieval extends BusinessRetrieval {

	public int getRequestType() {
		return Meeting;
	}

}
