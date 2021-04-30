package cn.flyrise.feep.retrieval.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2018-05-07 14:51
 */
public class RetrievalTypeRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "SearchRequest";
	}

	public String method;

	public RetrievalTypeRequest() {
		this.method = "getFunList";
	}
}
