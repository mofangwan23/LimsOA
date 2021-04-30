package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.meeting7.ui.bean.MeetingAttendUser;
import cn.flyrise.feep.meeting7.ui.bean.MeetingReply;
import cn.flyrise.feep.meeting7.ui.bean.QRCode;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-06-22 09:33
 */
public class MeetingDetailResponse extends ResponseContent {

	@SerializedName("meeting_join_id") public String meetingJoinId;         // 参会Id
	@SerializedName("initiatorUserId") public String initiatorId;           // 会议发起人Id
	@SerializedName("meetingMaster") public String meetingCompere;          // 会议主持人
	@SerializedName("meetingMasterId") public String meetingCompereId;      // 会议主持人Id
	@SerializedName("recordMan") public String meetingRecorder;             // 会议记录人
	@SerializedName("recordManId") public String meetingRecorderId;         // 会议记录人 Id
	@SerializedName("meetingAttendUsers") public List<MeetingAttendUser> attendUsers; //0:未处理,1:参加,2:不参加

	public String meetingId;            // 会议Id
	public String topics;               // 会议主题
	public String initiator;            // 会议发起人
	public String roomId;               // 会议室Id, 自定义会议该值为 ""
	public String roomName;             // 会议地点
	public String meetingAddress;       // 会议地址
	public String meetingType;          // 会议类型
	public String meetingTypeId;        // 会议类型Id
	public String meetingStatus;        // 参会状态 // -1:已过期,0:未处理,1:参加,2:不参加,3:已取消
	public String startDate;            // 开始时间
	public String endDate;              // 结束时间
	public String content;              // 会议内容
	public String attachmentGUID;       // 附件GUID
	public QRCode qrCode;               // 二维码
	public List<AttachmentBean> attachments;
	public List<MeetingReply> replies;

}
