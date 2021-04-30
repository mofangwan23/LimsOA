package cn.flyrise.feep.meeting7.ui.bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-06-26 15:12
 */
public class MeetingRoomData {

	public int page;
	public int size;
	public int totalNum;
	public int totalPage;
	public boolean hasNextPage;
	@SerializedName("list") public List<MeetingRoomDescription> meetingRooms;

}
