package cn.flyrise.android.protocol.entity.schedule;


import cn.flyrise.feep.core.network.request.RequestContent;
import com.google.gson.annotations.SerializedName;

/**
 * Created by yj on 2016/7/13.
 */
public class AgendaReplyRequest extends RequestContent {

	public static final String NAMESPACE = "AgendaRequest";
	private final String method = "reply";

	private String eventId;       //日程的ID
	private String replyContent;  //回复内容
	private String eventTitle;   //日程标题
	private String arrangerId;   //日程创建人

	public AgendaReplyRequest(String eventId, String replyContent, String eventTitle, String arrangerId) {
		this.eventId = eventId;
		this.replyContent = replyContent;
		this.eventTitle = eventTitle;
		this.arrangerId = arrangerId;
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}
}
