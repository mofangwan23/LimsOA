package com.hyphenate.chatui.group.model;

import android.support.annotation.Keep;

/**
 * Created by klc on 2018/2/2.
 */

@Keep
public class GroupSetting {

	private boolean allowInvite;

	public boolean isAllowInvite() {
		return allowInvite;
	}

	public void setAllowInvite(boolean allowInvite) {
		this.allowInvite = allowInvite;
	}
}
