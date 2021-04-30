package cn.flyrise.android.protocol.entity.location;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.location.bean.SignInLeaderDayDetail;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-15-11:29.
 * 领导日统计/统计明细
 */

public class SignInLeadeDayStatisDetailResponse extends ResponseContent {

	public List<SignInLeaderDayDetail> data;

}
