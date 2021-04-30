package cn.flyrise.android.protocol.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-03-23 15:57
 */
public class CommonGroup {

	@SerializedName("id") public String groupId;
	public String groupName;
	public String createUserId; // 这个干嘛的？看起来好像没什么卵用

}
