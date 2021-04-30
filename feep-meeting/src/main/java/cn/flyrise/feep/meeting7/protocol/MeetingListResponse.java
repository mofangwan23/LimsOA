package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.meeting7.ui.bean.MeetingEntity;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-06-22 09:29
 */
public class MeetingListResponse extends ResponseContent {

	public int untreated;           // 未处理条数
	public int overTotalPage;       // 往上翻有多少页
	public int totalPage;           // 往下翻有多少页
	public List<MeetingEntity> data;

}
