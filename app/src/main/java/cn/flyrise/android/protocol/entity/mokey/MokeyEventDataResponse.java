package cn.flyrise.android.protocol.entity.mokey;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by klc on 2018/3/13.
 * 获取挑战数据响应
 */

public class MokeyEventDataResponse extends ResponseContent {

	Data data;

	public class Data {

		String eventData;

		public String getEventData() {
			return eventData;
		}
	}

	public Data getData() {
		return data;
	}
}

