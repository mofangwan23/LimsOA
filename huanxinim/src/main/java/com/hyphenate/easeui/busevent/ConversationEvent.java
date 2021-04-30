package com.hyphenate.easeui.busevent;

/**
 * @author ZYP
 * @since 2017-08-11 14:14
 */
public interface ConversationEvent {

	class ChattingStartEvent {

		public ChattingStartEvent(String toChatUserId) {
			this.toChatUserId = toChatUserId;
		}

		public ChattingStartEvent() {
		}

		public String toChatUserId;
	}

	class ChattingEndEvent {

	}

}
