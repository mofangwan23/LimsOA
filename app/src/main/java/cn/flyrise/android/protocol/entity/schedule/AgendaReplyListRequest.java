package cn.flyrise.android.protocol.entity.schedule;


import cn.flyrise.feep.core.network.request.RequestContent;
import com.google.gson.annotations.SerializedName;

/**
 * Created by yj on 2016/7/13.
 */
public class AgendaReplyListRequest extends RequestContent {

	public static final String NAMESPACE = "AgendaRequest";
	private final String method = "acquireReply";

	private String eventId;

	public AgendaReplyListRequest(String eventId) {
		this.eventId = eventId;
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}
}
