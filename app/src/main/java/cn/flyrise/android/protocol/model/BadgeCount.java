package cn.flyrise.android.protocol.model;

import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * 菜单消息提醒气泡类
 * @author Robert
 */
public class BadgeCount {

	private String type = "-9";
	private String totalNums = "0";
	private String circleNums = "0";

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTotalNums() {
		return totalNums;
	}

	public void setTotalNums(String totalNums) {
		this.totalNums = totalNums;
	}

	public boolean isMessageType() {
		return type != null && "20".equals(type);
	}

	public int getTotalCount() {
		return CommonUtil.parseInt(totalNums);
	}

	public void setCircleNums(String circleNums){
		this.circleNums = circleNums;
	}

	public int getCircleNums(){
		return CommonUtil.parseInt(circleNums);
	}

}
