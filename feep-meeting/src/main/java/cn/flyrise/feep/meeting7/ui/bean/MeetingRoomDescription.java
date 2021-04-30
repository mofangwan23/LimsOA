package cn.flyrise.feep.meeting7.ui.bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-06-26 15:15
 */
public class MeetingRoomDescription {

	@SerializedName("id") public String roomId;
	public String address;
	public String name;
	public String status;
	@SerializedName("appointment") public List<Quantum> usableQuantums;

}
