package cn.flyrise.android.protocol.entity.location;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.location.bean.SignInMonthStatisItem;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-15-11:29.
 * 个人月统计
 */

public class SignInMonthStatisResponse extends ResponseContent {

	public List<SignInMonthStatisItem> data;
	;

}
