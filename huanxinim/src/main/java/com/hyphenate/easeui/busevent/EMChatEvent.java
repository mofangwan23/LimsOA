package com.hyphenate.easeui.busevent;

/**
 * @author KLC
 * @since 2017-08-16 10:00
 */
public interface EMChatEvent {

	class BaseGroupEvent {

		public String groupId;

		public BaseGroupEvent(String groupId) {
			this.groupId = groupId;
		}

		public String getGroupId() {
			return groupId;
		}
	}

	class GroupDestroyed extends BaseGroupEvent {

		private boolean initiative;

		public GroupDestroyed(String groupId, boolean initiative) {
			super(groupId);
			this.initiative = initiative;
		}

		public boolean isInitiative() {
			return initiative;
		}
	}

	class UserRemove extends BaseGroupEvent {

		private boolean initiative;

		public UserRemove(String groupId, boolean initiative) {
			super(groupId);
			this.initiative = initiative;
		}

		public boolean isInitiative() {
			return initiative;
		}
	}

}
