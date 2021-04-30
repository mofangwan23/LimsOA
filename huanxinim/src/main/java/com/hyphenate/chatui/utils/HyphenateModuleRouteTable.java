package com.hyphenate.chatui.utils;

import cn.squirtlez.frouter.IRouteTable;
import cn.squirtlez.frouter.RouteManager;
import com.hyphenate.chatui.retrieval.GroupSearchActivity;
import com.hyphenate.chatui.retrieval.MoreRecordSearchActivity;
import com.hyphenate.chatui.ui.ChatActivity;
import com.hyphenate.chatui.ui.ChatRecordSearchActivity;

/**
 * @author ZYP
 * @since 2017-12-06 11:53
 */
public class HyphenateModuleRouteTable implements IRouteTable {

	@Override public void registerTo(RouteManager manager) {
		manager.register("/im/single/detail", com.hyphenate.chatui.ui.SingleDetailActivity.class);
		manager.register("/im/chat", ChatActivity.class);
		manager.register("/im/chat/search", ChatRecordSearchActivity.class);
		manager.register("/im/chat/search/more", MoreRecordSearchActivity.class);
		manager.register("/im/chat/search/group", GroupSearchActivity.class);
	}
}
