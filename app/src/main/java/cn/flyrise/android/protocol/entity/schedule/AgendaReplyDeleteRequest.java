package cn.flyrise.android.protocol.entity.schedule;


import cn.flyrise.feep.core.network.request.RequestContent;
import com.google.gson.annotations.SerializedName;

/**
 * Created by yj on 2016/7/13.
 */
public class AgendaReplyDeleteRequest extends RequestContent {

	public static final String NAMESPACE = "AgendaRequest";
	private final String method = "delReply";

	private String id;
	private String eventTitle;
	private String arrangerId;
	private String eventId;

	public AgendaReplyDeleteRequest(String id, String eventTitle, String arrangerId, String eventId) {
		this.id = id;
		this.eventTitle = eventTitle;
		this.arrangerId = arrangerId;
		this.eventId = eventId;
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}
}
