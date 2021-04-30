package com.hyphenate.easeui.busevent;

import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;

import java.security.acl.Group;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-08-11 14:08
 */
public interface EMMessageEvent {

	class CmdChangeGroupName {

		public EMGroup group;

		public CmdChangeGroupName(EMGroup group) {
			this.group = group;
		}
	}

	class CmdGroupSettingUpdate {

		public String groupID;

		public CmdGroupSettingUpdate(String groupID) {
			this.groupID = groupID;
		}
	}


	class ImMessageRefresh {

		public List<EMMessage> messages;

		public ImMessageRefresh(List<EMMessage> messages) {
			this.messages = messages;
		}

	}

}
