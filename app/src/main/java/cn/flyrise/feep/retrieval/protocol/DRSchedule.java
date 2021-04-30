package cn.flyrise.feep.retrieval.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-09 17:46
 */
public class DRSchedule {

	/**
	 * "awokeTime": 30,
	 * "callbacks": [{}],
	 * "detail": "<p>aa<\/p>",
	 * "eventSource": "fe.do?SYS.ACTION=viewevent&SYS.ID=017-001-000",
	 * "eventSourceId": "2075",
	 * "file": "",
	 * "id": 12503,
	 * "isSharedEvent": "",
	 * "meetingAtte": "",
	 * "noteAwoke": "1",
	 * "period": "0",
	 * "periodStartDate": "",
	 * "periodStopDate": "",
	 * "resId": 5577,
	 * "richengShareId": "",
	 * "startTime": "2018-05-08 20:35:00",
	 * "stopTime": "2018-05-09 20:35:00",
	 * "title": "aa",
	 * "type": "3"
	 **/

	public String id;
	public String title;
	@SerializedName("detail") public String content;
	public String eventSource;
	public String eventSourceId;
	@SerializedName("resId") public String userId;
	@SerializedName("meetingAtte") public String meetingId;

}
