package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

public class MeetingPromptTimeRequest extends RequestContent {

	public String prompt;
	public String key;

	@Override public String getNameSpace() {
		return "PromptRequest";
	}

	public MeetingPromptTimeRequest() {
		this.key = "";
		this.prompt = "提醒周期";
	}

}
