package cn.flyrise.feep.meeting7.ui.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-06-27 15:45
 */
public class MeetingRoomDetailData {

	public String name;
	public String address;

	public String remark;
	public String seats;
	public String status;

	@SerializedName("admin") public String adminId;
	@SerializedName("equipMent") public String settings;

}
