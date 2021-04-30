package cn.flyrise.feep.event;

import java.util.List;

public class EventCircleMessageRead {

	private String value;
	private List<String> messageIds;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getMessageIds() {
		return messageIds;
	}

	public void setMessageIds(List<String> messageIds) {
		this.messageIds = messageIds;
	}
}
