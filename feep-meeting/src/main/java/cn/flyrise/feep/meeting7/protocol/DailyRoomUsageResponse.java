package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.meeting7.ui.bean.RoomUsageData;

/**
 * @author ZYP
 * @since 2018-06-29 09:45
 * 会议当天使用情况
 */
public class DailyRoomUsageResponse extends ResponseContent {

	public RoomUsageData data;

}
