package cn.flyrise.feep.retrieval.bean;

import cn.flyrise.feep.core.common.X.RequestType;

/**
 * @author ZYP
 * @since 2018-04-28 15:32
 */
public class NewsRetrieval extends BusinessRetrieval {

	public int getRequestType() {
		return RequestType.News;
	}

}
