package cn.flyrise.android.protocol.entity.location;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.location.bean.SignInLeaderMonthDetail;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-15-11:29.
 * 领导月统计详情
 */

public class SignInLeaderMonthStatisDetailResponse extends ResponseContent {

	public List<SignInLeaderMonthDetail> data;

}
