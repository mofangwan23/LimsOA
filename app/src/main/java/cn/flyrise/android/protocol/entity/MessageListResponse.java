package cn.flyrise.android.protocol.entity;

import java.util.List;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.main.message.MessageVO;

public class MessageListResponse extends ResponseContent {

	private List<MessageVO> results;
	private String totalNums;
	private int messageType = -1;

	public String getTotalNums() {
		return totalNums;
	}

	public void setTotalNums(String totalNums) {
		this.totalNums = totalNums;
	}

	public List<MessageVO> getResults() {
		return results;
	}

	public int getMessageType() {
		return messageType;
	}
}
