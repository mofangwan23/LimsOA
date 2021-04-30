package cn.flyrise.feep.meeting7.ui.bean;

/**
 * @author ZYP
 * @since 2018-06-22 09:30
 */

//    "data": [{
//        "meetingId": 755，                 // 会议Id
//        "topics": "高菲预约的会议",          // 标题
//        "initiator": "高菲",                // 发起人员
//
//        "startDate": "2018-06-14 08:30",   // 开始时间
//        "endDate": "2018-06-14 18:00",     // 结束时间
//        "roomName": "小会议室",             // 会议地点
//        "attendedFlag": "1"                // -1:已过期, 0:未处理, 1:参加, 2:不参加, 3:已取消
//    }]

public class MeetingEntity {

	public String meetingId;
	public String topics;
	public String initiator;
	public String startDate;
	public String endDate;
	public String roomName;
	public String attendedFlag;

}
