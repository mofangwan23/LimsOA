package com.hyphenate.easeui;

/**
 * Created by klc on 2017/3/20.
 */

public interface EaseUiK {

	interface EmUserList {

		int em_userList_delete_code = 0X101;           //用户列表界面移除群聊人员
		int em_userList_showAll_code = 0X102;          //用户列表显示所有群员
		int em_userList_changeOwner_code = 0X104;      //用户列表界面转换群主
		int em_userList_manage_code = 0X103;           //用户列表界面禁言管理

		String emGroupID = "Extra_groupID";                //用户列表群ID
		String emUserListType = "Extra_UserList_Event";  //用户列表操作事件类型
		String emUserListTitle = "Extra_title";  //用户列表操作事件类型

		String em_userList_addUser = "action_add_user";//允许添加人员
		String em_userList_removeUser = "action_remove_user";//允许删除人员
	}

	interface EmChatContent {

		String emChatID = "Extra_chatID";                  //聊天发起界面，单聊为userID，群聊为groupID
		String emChatType = "Extra_chatType";              //聊天发起类型

		int em_chatType_single = 0X103;                    //聊天发起类型  单聊
		int em_chatType_group = 0X104;                     //聊天发起类型  群聊

		String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
		String MESSAGE_ATTR_IS_VIDEO_CALL = "is_video_call";
		String MESSAGE_ATTR_IS_BIG_EXPRESSION = "em_is_big_expression";
		String MESSAGE_ATTR_EXPRESSION_ID = "em_expression_id";
		String MESSAGE_ATTR_AT_MSG = "em_at_list";
		String MESSAGE_ATTR_VALUE_AT_MSG_ALL = "ALL";
		String MESSAGE_ATTR_IS_SYSTEM = "is_system";
		String MESSAGE_ATTR_IS_REJECTION = "em_is_rejection";
		String MESSAGE_ATTR_NOT_SHOW_CONTENT = "em_not_show_content";

		String MESSAGE_COPY_GROUP = "is_copy_group";//是否为导出的群聊记录
		String MESSAGE_COPY_GROUP_USER_ID = "copy_group_user_id";//导出群聊记录时，缓存发送人

		String MESSAGE_SUPER_MOUDULE = "EM_NEW_MOUDULE_TYPE";

		//回复
		String MESSAGE_ATTR_IS_REPLY = "EM_IS_REPLY";
		String MESSAGE_ATTR_TIME = "EM_ATTR_TIME";

		//CMD 透传消息的action
		String CMD_ACTION_CHANGE_GROURPNAME = "CMD_ACTION_CHANGE_GROURPNAME";
		String CMD_ACTION_RECALL = "CMD_ACTION_RECALL";//撤销消息
		String CMD_ACTION_VIDEOCALL = "CMD_ACTION_VIDEOCALL_OFFLINE";
		String CMD_ACTION_VOICECALL = "CMD_ACTION_VOICECALL_OFFLINE";
		String CMD_ACTION_UPDATE_GROUP_SETTING = "CMD_ACTION_UPDATE_GROUP_SETTING";//通知更新群聊设置
		String CMD_ACTION_MARKED_READ = "CMD_ACTION_MARKED_READ";//标记多端自己已读
		String CMD_ACTION_FE_APP = "feApp";//将OA事项消息以透传消息的形式传过来，防止离线消息冲突

		//CMD 透传消息的属性
		String CMD_GROUPNAME = "CMD_GROUPNAME";
		String CMD_MSGID = "MSGID";//消息Id
		String CMD_ADD_USERLIST = "ADD_GROUP_MEMBER";

		//pc端接收来电消息
		String CMD_ACTION_PC_VOICE_CALL = "pcVoiceCall";
		String CMD_ACTION_PC_VIDEO_CALL = "pcVideoCall";

	}


}
