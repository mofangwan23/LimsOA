package cn.flyrise.android.protocol.entity.schedule;

import cn.flyrise.feep.core.network.request.RequestContent;
import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-11 10:35
 */
public class ScheduleSearchRequest extends RequestContent {

	// 对应 AgendaResponse.

	@Override public String getNameSpace() {
		return "AgendaRequest";
	}

	@SerializedName("page") public String pageNumber;
	@SerializedName("perPageNums") public String pageSize;
	@SerializedName("keyWord") public String keyword;

}
