package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author klc
 * @version 会议签到
 */
public class MeetingSignInRequest extends RequestContent {

	private String method = "MeetingScanSign";

	private String id;
	private String latitude;
	private String longitude;
	private String signPlace;


	public MeetingSignInRequest(String id, String latitude, String longitude, String signPlace) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.signPlace = signPlace;
	}

	@Override
	public String getNameSpace() {
		String NAMESPACE = "MeetingScanRequest";
		return NAMESPACE;
	}

}
