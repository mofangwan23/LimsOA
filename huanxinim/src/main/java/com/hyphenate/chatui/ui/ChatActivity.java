package com.hyphenate.chatui.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.SwipeBackLayout;
import cn.flyrise.feep.core.common.AndroidBug5497Workaround;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.squirtlez.frouter.FRouter;
import cn.squirtlez.frouter.annotations.Route;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.GroupDestroySettingActivity;
import com.hyphenate.chatui.group.GroupDetailsActivity;
import com.hyphenate.chatui.utils.EmHelper;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import com.hyphenate.easeui.busevent.EMMessageEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Route("/im/chat")
public class ChatActivity extends BaseChatActivity {

	public static final int ITEM_FILE = 1005;
	public static final int ITEM_COLLABORATION = 1008;  // 协同
	public static final int ITEM_EMAIL = 1009;          // 邮件
	public static final int ITEM_PLAN = 1010;           // 计划
	public static final int ITEM_FLOW = 1011;           // 流程
	public static final int ITEM_SCHEDULE = 1012;       // 日程

	// 权限返回值不能大于255
	public static final int ITEM_VIDEO = 161;
	public static final int ITEM_VOICE_CALL = 162;
	public static final int ITEM_VIDEO_CALL = 163;

	private ChatFragment chatFragment;
	private String mToChatUserId;
	private FEToolbar mToolBar;
	private int chatType;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		EventBus.getDefault().register(this);
		supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		setContentView(R.layout.em_activity_chat);
		if (VERSION.SDK_INT == VERSION_CODES.KITKAT && !DevicesUtil.isSpecialDevice()) {
			AndroidBug5497Workaround.assistActivity(this);
		}
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		mToolBar = toolbar;
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !DevicesUtil.isSpecialDevice()) {
			int statusBarHeight = DevicesUtil.getStatusBarHeight(this);
			this.mToolBar.setPadding(0, statusBarHeight, 0, 0);
		}

		mToolBar.setRightImageClickListener(v -> {
			if (chatType == EaseUiK.EmChatContent.em_chatType_single) {
				FRouter.build(ChatActivity.this, "/im/single/detail")
						.withString("userId", mToChatUserId)
						.go();
			}
			else {
				EMGroup group = EMClient.getInstance().groupManager().getGroup(mToChatUserId);
				Intent intent = new Intent();
				intent.putExtra("groupTitle", CoreZygote.getConvSTServices().getCoversationName(mToChatUserId));
				intent.putExtra("groupId", mToChatUserId);
				intent.setClass(ChatActivity.this
						, group == null ? GroupDestroySettingActivity.class : GroupDetailsActivity.class);
				startActivity(intent);
			}
		});
		mToolBar.setNavigationOnClickListener(v -> chatFragment.onBackPressed());
	}

	@Override
	public void bindData() {
		super.bindData();
		mToChatUserId = getIntent().getExtras().getString(EaseUiK.EmChatContent.emChatID);
		if (TextUtils.isEmpty(mToChatUserId)) {
			finish();
		}
		EmHelper.getInstance().setToChatUserID(mToChatUserId);
		chatFragment = new ChatFragment();
		chatFragment.setArguments(getIntent().getExtras());
		getSupportFragmentManager().beginTransaction().add(R.id.container, chatFragment).commit();
		mToolBar.setRightIcon(chatType == EaseUiK.EmChatContent.em_chatType_single ? R.drawable
				.ease_to_single_normal : R.drawable.ease_to_group_details_normal);
		chatType = getIntent().getIntExtra(EaseUiK.EmChatContent.emChatType, EaseUiK.EmChatContent.em_chatType_single);
		if (chatType == EmChatContent.em_chatType_single) {
			mToolBar.setRightIcon(R.drawable.ease_to_single_normal);
			CoreZygote.getAddressBookServices().queryUserDetail(mToChatUserId)
					.subscribe(f -> {
						mToolBar.setTitle(f == null ? mToChatUserId : f.name);
					}, error -> {
						mToolBar.setTitle(mToChatUserId);
					});
		}
		else {
			mToolBar.setRightIcon(R.drawable.ease_to_group_details_normal);
			EMGroup emGroup = EMClient.getInstance().groupManager().getGroup(mToChatUserId);
			mToolBar.setTitle(emGroup == null ? CoreZygote.getConvSTServices().getCoversationName(mToChatUserId) : emGroup.getGroupName());
		}
	}


	@Override
	protected void onNewIntent(Intent intent) {
		String username = intent.getStringExtra(EaseUiK.EmChatContent.emChatID);
		if (!TextUtils.isEmpty(username)) {
			if (mToChatUserId.equals(username)) {
				String forward_msg_id = intent.getStringExtra("forward_msg_id");
				if (forward_msg_id != null) {
					chatFragment.forwardMessage(forward_msg_id);
				}
				super.onNewIntent(intent);
			}
			else {
				finish();
				startActivity(intent);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (chatFragment != null) {
			chatFragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onBackPressed() {
		chatFragment.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		EMConversation conversations = EMClient.getInstance().chatManager().getConversation(mToChatUserId);
		if (conversations == null) {
			finish();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCmdGroupNameChange(EMMessageEvent.CmdChangeGroupName event) {
		EMGroup group = event.group;
		if (mToChatUserId.equals(group.getGroupId())) {
			mToolBar.setTitle(group.getGroupName());
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSwipeBackEnableEvent(Integer code) {
		if (code == SwipeBackLayout.ENABLE_SWIPE) {
			if (mSwipeBackLayout != null) {
				mSwipeBackLayout.setAbleToSwipe(true);
			}
		}
		else if (code == SwipeBackLayout.DISABLE_SWIPE) {
			if (mSwipeBackLayout != null) {
				mSwipeBackLayout.setAbleToSwipe(false);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		EmHelper.getInstance().setToChatUserID(null);
	}
}
