package cn.flyrise.android.protocol.entity.schedule;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.schedule.model.ScheduleReply;
import java.util.List;

/**
 * Created by klc on 2018/3/27.
 */

public class AgendaReplyListResponse extends ResponseContent {

	List<ScheduleReply> data;


	public List<ScheduleReply> getData() {
		return data;
	}
}
