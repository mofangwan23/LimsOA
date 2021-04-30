package cn.flyrise.feep.robot.bean;

import com.google.gson.annotations.SerializedName;

/**
 * 新建：陈冕;
 * 日期： 2018-8-28-11:16.
 */
public class RobotWorkPlanListItemBean {

	public String id;
	public String title;
	public String sendUser;
	public String sendTime;
	public String sectionName;
	@SerializedName("UserId")
	public String sendUserId;
	public String status;
	public String content;
	public String badge;

	public boolean isNews;

	public String type;//计划类型 1:日计划,2:周计划,3:月计划,4:其他计划
}
