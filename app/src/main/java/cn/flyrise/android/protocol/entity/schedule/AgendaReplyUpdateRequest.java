package cn.flyrise.android.protocol.entity.schedule;


import cn.flyrise.feep.core.network.request.RequestContent;
import com.google.gson.annotations.SerializedName;

/**
 * Created by yj on 2016/7/13.
 */
public class AgendaReplyUpdateRequest extends RequestContent {

	public static final String NAMESPACE = "AgendaRequest";
	private final String method = "updateReply";

	private String id;
	private String replyContent;
	private String eventTitle;
	private String arrangerId;
	private String eventId;


	public AgendaReplyUpdateRequest(String id, String replyContent, String eventTitle, String arrangerId, String eventId) {
		this.id = id;
		this.replyContent = replyContent;
		this.eventTitle = eventTitle;
		this.arrangerId = arrangerId;
		this.eventId = eventId;
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

}
