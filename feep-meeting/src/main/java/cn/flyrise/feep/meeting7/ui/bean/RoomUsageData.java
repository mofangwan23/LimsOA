package cn.flyrise.feep.meeting7.ui.bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-06-29 09:55
 */
public class RoomUsageData {

	@SerializedName("use") public List<RoomUsage> usages;
	@SerializedName("id") public String roomId;
	@SerializedName("name") public String roomName;

}
