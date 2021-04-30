package cn.flyrise.android.protocol.entity.schedule;

import cn.flyrise.feep.core.network.request.ResponseContent;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-05-11 11:27
 */
public class ScheduleSearchResponse extends ResponseContent {

	public int totalNums;

	public List<AgendaResponseItem> items;

}
