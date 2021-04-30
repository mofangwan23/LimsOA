package com.hyphenate.easeui.listener;

import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMMucSharedFile;

import java.util.List;

/**
 * @author klc
 * @since 2017-07-26 16:07
 */
public class SimpleGroupChangeListener implements EMGroupChangeListener {


	/**
	 * 当前用户收到加入群组邀请
	 * @param groupId groupId	要加入的群的id
	 * @param groupName groupName	要加入的群的名称
	 * @param inviter inviter	邀请人的id
	 * @param reason reason	邀请加入的reason
	 */
	@Override
	public void onInvitationReceived(String groupId,
			String groupName,
			String inviter,
			String reason) {

	}

	/**
	 * @param groupId 要加入的群的id
	 * @param groupName 要加入的群的名称
	 * @param applicant 申请人的username
	 * @param reason 申请加入的reason
	 */
	@Override
	public void onRequestToJoinReceived(String groupId,
			String groupName,
			String applicant,
			String reason) {

	}

	/**
	 *加群申请被对方接受
	 * @param groupId 群组的id
	 * @param groupName 群组的名字
	 * @param accepter 同意人得username
	 */
	@Override
	public void onRequestToJoinAccepted(String groupId,
			String groupName,
			String accepter) {
	}

	/**
	 * 加群申请被拒绝
	 * @param groupId 群组id
	 * @param groupName 群组名字
	 * @param decliner 拒绝人得username
	 * @param reason 拒绝理由
	 */
	@Override
	public void onRequestToJoinDeclined(String groupId,
			String groupName,
			String decliner,
			String reason) {

	}


	/**
	 * 群组邀请被接受
	 * @param groupId
	 * @param invitee
	 * @param reason
	 */
	@Override
	public void onInvitationAccepted(String groupId,
			String invitee,
			String reason) {

	}

	@Override
	public void onInvitationDeclined(String s, String s1, String s2) {

	}

	@Override
	public void onUserRemoved(String s, String s1) {

	}

	@Override
	public void onGroupDestroyed(String s, String s1) {

	}

	@Override
	public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {

	}

	@Override
	public void onMuteListAdded(String s, List<String> list, long l) {

	}

	@Override
	public void onMuteListRemoved(String s, List<String> list) {

	}

	@Override
	public void onAdminAdded(String s, String s1) {

	}

	@Override
	public void onAdminRemoved(String s, String s1) {

	}

	@Override
	public void onOwnerChanged(String s, String s1, String s2) {

	}

	@Override
	public void onMemberJoined(String s, String s1) {

	}

	@Override
	public void onMemberExited(String s, String s1) {

	}

	@Override
	public void onAnnouncementChanged(String s, String s1) {

	}

	@Override
	public void onSharedFileAdded(String s, EMMucSharedFile emMucSharedFile) {

	}

	@Override
	public void onSharedFileDeleted(String s, String s1) {

	}
}
