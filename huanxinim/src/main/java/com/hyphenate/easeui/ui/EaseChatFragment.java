package com.hyphenate.easeui.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.BitmapUtil;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.EmojiUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.media.record.CameraActivity;
import cn.flyrise.feep.media.record.MediaRecorder;
import cn.flyrise.feep.media.record.camera.JCameraView;
import cn.squirtlez.frouter.FRouter;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import com.hyphenate.easeui.busevent.ChatContent;
import com.hyphenate.easeui.busevent.EMChatEvent.BaseGroupEvent;
import com.hyphenate.easeui.busevent.EMChatEvent.GroupDestroyed;
import com.hyphenate.easeui.busevent.EMChatEvent.UserRemove;
import com.hyphenate.easeui.busevent.EMMessageEvent.ImMessageRefresh;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.model.ChatMessageProvider;
import com.hyphenate.easeui.model.EaseAtMessageHelper;
import com.hyphenate.easeui.model.SendEMCallBack;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.MMPMessageUtil;
import com.hyphenate.easeui.widget.EaseChatInputMenu;
import com.hyphenate.easeui.widget.EaseChatInputMenu.ChatInputMenuListener;
import com.hyphenate.easeui.widget.EaseChatMessageList;
import com.hyphenate.easeui.widget.EaseVoiceRecorderView;
import com.hyphenate.util.PathUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * you can new an EaseChatFragment to use or you can inherit it to expand.
 * You need call setArguments to pass chatType and userId
 * <br/>
 * <br/>
 * you can see ChatActivity in demo for your reference
 */
public class EaseChatFragment extends Fragment {

	protected static final String TAG = "EaseChatFragment";

	public static final int REQUEST_CODE_MAP = 1;
	public static final int REQUEST_CODE_CAMERA = 2;
	public static final int REQUEST_CODE_LOCAL = 3;
	public static final int REQUEST_CODE_SELETED_PICTURE = 4;

	public static final int ITEM_TAKE_PICTURE = 1001;
	public static final int ITEM_PICTURE = 1002;
	public static final int ITEM_LOCATION = 1003;

	protected EaseChatMessageList messageList;
	protected EaseChatInputMenu mInputMenu;
	protected View mChatContentView;
	protected View speakView;
	protected InputMethodManager inputManager;
	protected ClipboardManager clipboard;
	protected EaseVoiceRecorderView voiceRecorderView;

	protected Bundle fragmentArgs;
	protected int chatType;
	protected String toChatUsername;
	protected EMConversation conversation;
	protected File cameraFile;
	protected EMMessage contextMenuMessage;
	private ChatMessageProvider mDataProvider;

	private boolean isPause;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.ease_fragment_chat, container, false);
		fragmentArgs = getArguments();
		toChatUsername = fragmentArgs.getString(EaseUiK.EmChatContent.emChatID);
		chatType = fragmentArgs.getInt(EaseUiK.EmChatContent.emChatType, EaseUiK.EmChatContent.em_chatType_single);
		bindView(view);
		bindData();
		bindListener();
		return view;
	}

	/**
	 * init view
	 */
	protected void bindView(View view) {
		mChatContentView = view.findViewById(R.id.layoutContentView);
		voiceRecorderView = (EaseVoiceRecorderView) view.findViewById(R.id.voice_recorder);
		messageList = (EaseChatMessageList) view.findViewById(R.id.message_list);
		mInputMenu = (EaseChatInputMenu) view.findViewById(R.id.input_menu);
		inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	protected void bindData() {
		conversation = EMClient.getInstance().chatManager()
				.getConversation(toChatUsername, EaseCommonUtils.getConversationType(chatType), true);
		mInputMenu.setChatListView(mChatContentView);
		registerExtendMenuItem();
		mInputMenu.init(null);
		mDataProvider = new ChatMessageProvider(conversation);
		messageList.initData(conversation, mDataProvider);
		String forward_msg_id = getArguments().getString("forward_msg_id");
		if (forward_msg_id != null) {
			forwardMessage(forward_msg_id);
		}
	}

	@SuppressLint("ClickableViewAccessibility") protected void bindListener() {
		mInputMenu.setChatInputMenuListener(new ChatInputMenuListener() {
			@Override
			public void onSendMessage(String content) {
				String replyMessage = mInputMenu.getPrimaryMenu().getReplyMessage();
				if (!TextUtils.isEmpty(replyMessage)) {
					mInputMenu.getPrimaryMenu().hideReplyTextView();
					sendReplyMessage(content);
				}
				else {
					sendTextMessage(content);
				}
			}

			@Override
			public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
				if (!FePermissions.isPermissionGranted(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO})) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						speakView = v;
						v.setEnabled(false);
						requestRecordPermissions();
					}
					return true;
				}
				else {
					return voiceRecorderView.onPressToSpeakBtnTouch(v, event,
							(voiceFilePath, voiceTimeLength) -> sendVoiceMessage(voiceFilePath, voiceTimeLength));
				}
			}

			@Override
			public void onBigExpressionClicked(EaseEmojicon emojicon) {
				sendBigExpressionMessage(emojicon.getEmojiText(), emojicon.getIdentityCode());
			}
		});

		mInputMenu.setExtendMenuItemClickListener((id, view) -> onExtendMenuItemClick(id));

		messageList.getListView().setOnTouchListener((View v, MotionEvent event) -> {
			hideKeyboard();
			mInputMenu.hideMenuContainer();
			return false;
		});
	}

	/**
	 * register extend menu, item id need > 3 if you override this method and keep exist item
	 */
	protected void registerExtendMenuItem() {
		mInputMenu.registerExtendMenuItem(CommonUtil.getString(R.string.attach_take_pic),
				R.drawable.ease_chat_takepic_selector, 1001);                              // 1001 = ChatActivity.ITEM_TAKE_PICTURE
		mInputMenu.registerExtendMenuItem(CommonUtil.getString(R.string.attach_picture),
				R.drawable.ease_chat_image_selector, 1002);                                // 1002 = ChatActivity.ITEM_PICTURE
		mInputMenu.registerExtendMenuItem(CommonUtil.getString(R.string.attach_location),
				R.drawable.ease_chat_location_selector, 1003);                             // 1002 = ChatActivity.ITEM_LOCATION
	}

	protected void onExtendMenuItemClick(int itemId) {
		switch (itemId) {
			case ITEM_TAKE_PICTURE:                                             // 拍照
				FePermissions.with(this)
						.permissions(new String[]{Manifest.permission.CAMERA})
						.rationaleMessage(getResources().getString(R.string.permission_rationale_camera))
						.requestCode(PermissionCode.CAMERA)
						.request();
				break;
			case ITEM_PICTURE:                                                  // 选择照片
				FRouter.build(getActivity(), "/media/image/select")
						.withStrings("extra_except_path", new String[]{CoreZygote.getPathServices().getUserPath()})
						.requestCode(REQUEST_CODE_SELETED_PICTURE)
						.go();
				break;
			case ITEM_LOCATION:                                                 // 发送位置
				FePermissions.with(this)
						.permissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})
						.rationaleMessage(getResources().getString(R.string.permission_rationale_location))
						.requestCode(PermissionCode.LOCATION)
						.request();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_CAMERA) { // capture new image
				if (cameraFile != null && cameraFile.exists()) {
					sendImageMessage(cameraFile.getAbsolutePath());
				}
			}
			else if (requestCode == REQUEST_CODE_LOCAL) { // send local image
				if (data != null) {
					Uri selectedImage = data.getData();
					if (selectedImage != null) {
						sendPicByUri(selectedImage);
					}
				}
			}
			else if (requestCode == REQUEST_CODE_MAP) { // location
				double latitude = data.getDoubleExtra("latitude", 0);
				double longitude = data.getDoubleExtra("longitude", 0);
				String locationAddress = data.getStringExtra("address");
				String locationTitle = data.getStringExtra("poiTitle");
				if (locationAddress != null && !locationAddress.equals("")) {
					sendLocationMessage(latitude, longitude, locationTitle, locationAddress);
				}
				else {
					Toast.makeText(getActivity(), R.string.unable_to_get_loaction, Toast.LENGTH_SHORT).show();
				}
			}
			else if (requestCode == REQUEST_CODE_SELETED_PICTURE) {  // 选择图片
				if (data != null) {
					ArrayList<String> selectedPictures = data.getStringArrayListExtra("SelectionData");
					if (CommonUtil.nonEmptyList(selectedPictures)) {
						for (String picturePath : selectedPictures) {
							if (BitmapUtil.isPictureGif(picturePath)) {
								sendFileMessage(picturePath);
							}
							else {
								sendImageMessage(picturePath);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		messageList.setActivityPause(false);
		if (chatType == EaseUiK.EmChatContent.em_chatType_group) {
			EaseAtMessageHelper.get().removeAtMeGroup(toChatUsername);
		}
		conversation.markAllMessagesAsRead();
		isPause = false;
		messageList.refresh();
	}

	@Override
	public void onPause() {
		super.onPause();
		messageList.setActivityPause(true);
		isPause = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		mDataProvider.clearMemoryMsg();
	}

	public void onBackPressed() {
		if (mInputMenu.onBackPressed()) {
			getActivity().finish();
			if (chatType == EaseUiK.EmChatContent.em_chatType_group) {
				EaseAtMessageHelper.get().removeAtMeGroup(toChatUsername);
				EaseAtMessageHelper.get().cleanToAtUserList();
			}
		}
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageRefresh(ImMessageRefresh refresh) {
		if (isPause) {
			return;
		}
		List<EMMessage> messages = refresh.messages;
		for (EMMessage message : messages) {
			String username = message.getChatType() == ChatType.GroupChat ? message.getTo() : message.getFrom();
			if (username.equals(toChatUsername) || message.getFrom().equals(EMClient.getInstance().getCurrentUser())) {
				messageList.refresh();
				return;
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageRefresh(BaseGroupEvent event) {
		if (isPause) {
			return;
		}
		if (event.groupId.equals(toChatUsername)) {
			messageList.refresh();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onGroupDestroyed(GroupDestroyed event) {
		if (event.getGroupId().equals(toChatUsername)) {
			showGroupEventDialog(true, event.isInitiative());
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onUserRemoved(UserRemove event) {
		if (event.getGroupId().equals(toChatUsername)) {
			showGroupEventDialog(false, event.isInitiative());
		}
	}

	private void showGroupEventDialog(boolean isDesotry, boolean isInitiative) {
		if (EMClient.getInstance().getOptions().isDeleteMessagesAsExitGroup()) {
			String hint = isDesotry ? getString(R.string.group_destroy_msg) : getString(R.string.remove_group_other);
			if (isInitiative) {
				getActivity().finish();
			}
			else {
				new FEMaterialDialog.Builder(getActivity())
						.setMessage(hint)
						.setCancelable(false)
						.setPositiveButton(null,
								dialog -> getActivity().finish()).build().show();
			}
		}
		else {
			messageList.loadMoreMsg();
		}
	}


	@Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
	public void onSeekToEvent(ChatContent.SeekToMsgEvent event) {
		messageList.seekToMsg(event.msgID);
	}

	/**
	 * input @
	 */
	protected void inputAtUsername(String username, boolean autoAddAtSymbol) {
		if (EMClient.getInstance().getCurrentUser().equals(username)
				|| chatType != EaseUiK.EmChatContent.em_chatType_group) {
			return;
		}
		if (autoAddAtSymbol) {
			mInputMenu.insertText(username + " ");
			EaseAtMessageHelper.get().addAtUser(username);
		}
		else {
			mInputMenu.insertText(username + " ");
		}
	}


	//send message
	protected void sendTextMessage(String content) {
		if (TextUtils.isEmpty(content)) {
			return;
		}
		if (EaseAtMessageHelper.get().containsAtUsername(content)) {
			sendAtMessage(content);
		}
		else {
			EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
			sendMessage(message);
		}
	}

	/**
	 * send @ message, only support group chat message
	 */
	private void sendAtMessage(String content) {
		if (chatType != EaseUiK.EmChatContent.em_chatType_group) {
			return;
		}
		EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
		EMGroup group = EMClient.getInstance().groupManager().getGroup(toChatUsername);
		if (EMClient.getInstance().getCurrentUser().equals(group.getOwner()) && EaseAtMessageHelper.get().containsAtAll(content)) {
			message.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_AT_MSG, EaseUiK.EmChatContent.MESSAGE_ATTR_VALUE_AT_MSG_ALL);
		}
		else {
			message.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_AT_MSG, EaseAtMessageHelper.get().formatAtUserToString());
		}
		sendMessage(message);
	}

	protected void sendBigExpressionMessage(String name, String identityCode) {
		EMMessage message = EaseCommonUtils.createExpressionMessage(toChatUsername, name, identityCode);
		sendMessage(message);
	}

	protected void sendVoiceMessage(String filePath, int length) {
		EMMessage message = EMMessage.createVoiceSendMessage(filePath, length, toChatUsername);
		sendMessage(message);
	}

	protected void sendImageMessage(String imagePath) {
		EMMessage message = EMMessage.createImageSendMessage(imagePath, true, toChatUsername);
		sendMessage(message);
	}

	protected void sendLocationMessage(double latitude, double longitude, String locationTitle, String locationAddress) {
		EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, locationTitle, toChatUsername);
		message.setAttribute("locationAddress", locationAddress);
		sendMessage(message);
	}

	protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength) {
		EMMessage message = EMMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, toChatUsername);
		sendMessage(message);
	}

	protected void sendFileMessage(String filePath) {
		EMMessage message = EMMessage.createFileSendMessage(filePath, toChatUsername);
		sendMessage(message);
	}

	protected void sendReplyMessage(String content) {
		EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
		message.setAttribute(EmChatContent.MESSAGE_SUPER_MOUDULE, contextMenuMessage.getStringAttribute("type", ""));
		message.setAttribute("id", contextMenuMessage.getStringAttribute("id", ""));
		message.setAttribute("msgId", contextMenuMessage.getStringAttribute("msgId", ""));
		message.setAttribute("url", contextMenuMessage.getStringAttribute("url", ""));
		message.setAttribute("action", contextMenuMessage.getStringAttribute("action", ""));
		message.setAttribute("title", contextMenuMessage.getStringAttribute("title", ""));
		message.setAttribute("content", contextMenuMessage.getStringAttribute("content", ""));
		message.setAttribute(EmChatContent.MESSAGE_ATTR_TIME, contextMenuMessage.getMsgTime());
		message.setAttribute(EmChatContent.MESSAGE_ATTR_IS_REPLY, true);
		sendMessage(message);
	}

	protected String getExtendMsgContent() {
		String action = contextMenuMessage.getStringAttribute("action", "");
		String title = contextMenuMessage.getStringAttribute("title", "");
		String content = contextMenuMessage.getStringAttribute("content", "");
		return action.equals(title) ? content : title;
	}

//	protected void sendMessage(EMMessage message) {
//		if (message == null) return;
//		if (chatType == EaseUiK.EmChatContent.em_chatType_group) {
//			message.setChatType(ChatType.GroupChat);
//		}
//		message.setLocalTime(System.currentTimeMillis());
//		message.setMessageStatusCallback(new SendEMCallBack(message, null));
//		EMClient.getInstance().chatManager().sendMessage(message);
//		messageList.refreshSelectLast();
//	}

	protected void sendMessage(EMMessage message) {
		if (chatType == EmChatContent.em_chatType_group) {
			message.setChatType(ChatType.GroupChat);
		}
		message.setAttribute("em_apns_ext", getOfflineMessageJson(getEmMessageText(message)));// 将推送扩展设置到消息中
		message.setAttribute("em_force_notification", true);//强制推送

		message.setLocalTime(System.currentTimeMillis());
		message.setMessageStatusCallback(new SendEMCallBack(message, null));
		EMClient.getInstance().chatManager().sendMessage(message);
		messageList.refreshSelectLast();
	}

	private String getEmMessageText(EMMessage message) {//显示在离线通知栏上的消息
		if (message == null) return getString(R.string.chat_messge_hint);
		String notifyText = "";
		if (message.getBody() instanceof EMTextMessageBody) {
			notifyText = ((EMTextMessageBody) message.getBody()).getMessage();
			notifyText = EmojiUtil.parseEmojiText(notifyText);
		}
		else if (message.getBody() instanceof EMImageMessageBody) {
			notifyText = "发来一张图片";
		}
		else if (message.getBody() instanceof EMNormalFileMessageBody) {
			notifyText = "发来一个文件";
		}
		else if (message.getBody() instanceof EMVoiceMessageBody) {
			notifyText = "发来一条语音";
		}
		else if (message.getBody() instanceof EMLocationMessageBody) {
			notifyText = "共享一个位置";
		}
		else if (message.getBody() instanceof EMVideoMessageBody) {
			notifyText = "发来一条视频";
		}
		return TextUtils.isEmpty(notifyText) ? getString(R.string.chat_messge_hint) : notifyText;
	}

	private JSONObject getOfflineMessageJson(String text) {// 将推送扩展设置到消息中
		// 设置自定义推送提示
		JSONObject extObject = new JSONObject();
		try {
			extObject.put("em_push_name", "食药协作平台");
			extObject.put("em_push_content", getMessageTitle() + ":" + text);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return extObject;
	}

	private String getMessageTitle() {
		String title = "";
		if (chatType == EmChatContent.em_chatType_single) {
			title = CoreZygote.getLoginUserServices().getUserName();
		}
		else {
			EMGroup emGroup = EMClient.getInstance().groupManager().getGroup(toChatUsername);
			title = emGroup == null ? CoreZygote.getConvSTServices().getCoversationName(toChatUsername) : emGroup.getGroupName();
		}
		return TextUtils.isEmpty(title) ? "TouchC" : title;
	}

	protected void inputReplyMsg() {
		String msg = "“" + getExtendMsgContent();
		mInputMenu.getPrimaryMenu().setReplyMessage(msg);
	}

	public void resendMessage(EMMessage message) {
		message.setStatus(EMMessage.Status.CREATE);
		EMClient.getInstance().chatManager().sendMessage(message);
		messageList.refresh();
	}

	public void sendRecallCmdMsg(EMMessage message) {
		EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
		String action = EmChatContent.CMD_ACTION_RECALL;
		EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
		cmdMsg.setChatType(chatType == EmChatContent.em_chatType_single ? ChatType.Chat : ChatType.GroupChat);
		cmdMsg.setTo(message.getTo());
		cmdMsg.setFrom(EMClient.getInstance().getCurrentUser());
		cmdMsg.setMsgTime(message.getMsgTime());
		cmdMsg.setLocalTime(message.localTime());
		cmdMsg.setAttribute(EmChatContent.CMD_MSGID, message.getMsgId());
		cmdMsg.addBody(cmdBody);
		EMClient.getInstance().chatManager().sendMessage(cmdMsg);
		cmdMsg.setMessageStatusCallback(new EMCallBack() {
			@Override
			public void onSuccess() {
				EMClient.getInstance().chatManager().getConversation(message.getTo()).removeMessage(message
						.getMsgId());
				MMPMessageUtil.saveRecallMessage(getActivity(), message, true);
				messageList.refresh();
			}

			@Override
			public void onError(int i, String s) {
				FEToast.showMessage(R.string.recall_error);
			}

			@Override
			public void onProgress(int i, String s) {
			}
		});
	}

	//===================================================================================

	/**
	 * send image
	 */
	protected void sendPicByUri(Uri selectedImage) {
		String[] filePathColumn = {MediaStore.Images.Media.DATA};
		Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			if (picturePath == null || picturePath.equals("null")) {
				Toast toast = Toast.makeText(getActivity(), R.string.cant_find_pictures, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;
			}
			sendImageMessage(picturePath);
		}
		else {
			File file = new File(selectedImage.getPath());
			if (!file.exists()) {
				Toast toast = Toast.makeText(getActivity(), R.string.cant_find_pictures, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;

			}
			sendImageMessage(file.getAbsolutePath());
		}
	}

	public void requestRecordPermissions() {
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.RECORD_AUDIO})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
				.requestCode(PermissionCode.RECORD)
				.request();
	}

	@PermissionGranted(PermissionCode.LOCATION)
	public void onLocationPermissionGranted() {
		Bundle bundle = new Bundle();
		bundle.putInt("isSendLocation", 603);
		FRouter.build(getActivity(), "/location/selected").withBundle(bundle).requestCode(REQUEST_CODE_MAP).go();
	}


	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGranted() {
		cameraFile = new File(PathUtil.getInstance().getImagePath(), EMClient.getInstance().getCurrentUser()
				+ System.currentTimeMillis() + ".jpg");
		Intent intent = new Intent(getActivity(), CameraActivity.class);
		intent.putExtra("cameravew_path", cameraFile.getAbsolutePath());
		intent.putExtra("cameravew_state", JCameraView.BUTTON_STATE_ONLY_CAPTURE);
		startActivityForResult(intent, REQUEST_CODE_CAMERA);

//		startActivityForResult(
//				new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//						.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)), REQUEST_CODE_CAMERA);
	}

	@PermissionGranted(PermissionCode.RECORD)
	public void onRecordPermissionGranted() {
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
		if (speakView != null) {
			speakView.setEnabled(true);
		}
	}


	/**
	 * hide
	 */
	protected void hideKeyboard() {
		if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getActivity().getCurrentFocus() != null) {
				inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	/**
	 * forward message
	 */
	public void forwardMessage(String forward_msg_id) {
		final EMMessage forward_msg = EMClient.getInstance().chatManager().getMessage(forward_msg_id);
		EMMessage.Type type = forward_msg.getType();
		switch (type) {
			case TXT:
				if (forward_msg.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
					sendBigExpressionMessage(((EMTextMessageBody) forward_msg.getBody()).getMessage(),
							forward_msg.getStringAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_EXPRESSION_ID, null));
				}
				else {
					String content = ((EMTextMessageBody) forward_msg.getBody()).getMessage();
					sendTextMessage(content);
				}
				break;
			case IMAGE:
				// send image
				String filePath = ((EMImageMessageBody) forward_msg.getBody()).getLocalUrl();
				if (filePath != null) {
					File file = new File(filePath);
					if (!file.exists()) {
						// send thumb nail if original image does not exist
						filePath = ((EMImageMessageBody) forward_msg.getBody()).thumbnailLocalPath();
					}
					sendImageMessage(filePath);
				}
				break;
			default:
				break;
		}

		if (forward_msg.getChatType() == EMMessage.ChatType.ChatRoom) {
			EMClient.getInstance().chatroomManager().leaveChatRoom(forward_msg.getTo());
		}
	}

}
