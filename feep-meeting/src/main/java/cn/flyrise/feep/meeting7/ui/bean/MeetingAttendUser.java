package cn.flyrise.feep.meeting7.ui.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-06-22 09:44
 */
public class MeetingAttendUser {

	@SerializedName("meetingAttendUserID") public String userId;
	@SerializedName("meetingAttendStatus") public String attendStatus;

}
