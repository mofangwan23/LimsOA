package com.hyphenate.chatui.ui;

import static cn.flyrise.feep.core.common.X.Func.Activity;
import static cn.flyrise.feep.core.common.X.Func.Announcement;
import static cn.flyrise.feep.core.common.X.Func.CRM;
import static cn.flyrise.feep.core.common.X.Func.CircleNotice;
import static cn.flyrise.feep.core.common.X.Func.Done;
import static cn.flyrise.feep.core.common.X.Func.InBox;
import static cn.flyrise.feep.core.common.X.Func.Knowledge;
import static cn.flyrise.feep.core.common.X.Func.Meeting;
import static cn.flyrise.feep.core.common.X.Func.News;
import static cn.flyrise.feep.core.common.X.Func.Plan;
import static cn.flyrise.feep.core.common.X.Func.Schedule;
import static cn.flyrise.feep.core.common.X.Func.Sended;
import static cn.flyrise.feep.core.common.X.Func.ToDo;
import static cn.flyrise.feep.core.common.X.Func.ToSend;
import static cn.flyrise.feep.core.common.X.Func.Trace;
import static cn.flyrise.feep.core.common.X.Func.Vote;
import static com.hyphenate.chatui.ui.ChatActivity.ITEM_COLLABORATION;
import static com.hyphenate.chatui.ui.ChatActivity.ITEM_EMAIL;
import static com.hyphenate.chatui.ui.ChatActivity.ITEM_FILE;
import static com.hyphenate.chatui.ui.ChatActivity.ITEM_FLOW;
import static com.hyphenate.chatui.ui.ChatActivity.ITEM_PLAN;
import static com.hyphenate.chatui.ui.ChatActivity.ITEM_SCHEDULE;
import static com.hyphenate.chatui.ui.ChatActivity.ITEM_VIDEO;
import static com.hyphenate.chatui.ui.ChatActivity.ITEM_VIDEO_CALL;
import static com.hyphenate.chatui.ui.ChatActivity.ITEM_VOICE_CALL;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Toast;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.EmojiUtil;
import cn.flyrise.feep.core.common.utils.SystemScheduleUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.request.NoticesManageRequest;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.media.record.CameraActivity;
import cn.flyrise.feep.media.record.MediaRecorder;
import cn.squirtlez.frouter.FRouter;
import cn.squirtlez.frouter.RouteCreator;
import com.borax12.materialdaterangepicker.DateTimePickerDialog;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.db.MessageSettingManager;
import com.hyphenate.chatui.domain.EmojiconExampleGroupData;
import com.hyphenate.chatui.domain.MessageSetting;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.model.Message;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseChatMessageList.MessageListItemClickListener;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenu;
import com.hyphenate.util.PathUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ChatFragment extends EaseChatFragment {

	public static final int REQUEST_CODE_SELECT_FILE = 12;
	public static final int REQUEST_CODE_SELECT_AT_USER = 15;

	private static final int RECALL_INTERVAL_TIME = 3 * 60 * 1000;
	private static final int FILE_MAX_SIZE = 10 * 1024 * 1024;

	private FELoadingDialog mLoadingDialog;

	@Override
	protected void bindData() {
		super.bindData();
		((EaseEmojiconMenu) mInputMenu.getmEmojiconMenu()).addEmojiconGroup(EmojiconExampleGroupData.getData());
	}

	@Override
	protected void bindListener() {
		super.bindListener();
		if (chatType == EaseUiK.EmChatContent.em_chatType_group) {
			mInputMenu.getPrimaryMenu().getEditText().addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (count == 1 && "@".equals(String.valueOf(s.charAt(start)))) {
						Intent intent = new Intent(getActivity(), PickAtUserActivity.class);
						intent.putExtra("groupId", toChatUsername);
						startActivityForResult(intent, REQUEST_CODE_SELECT_AT_USER);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});
		}
		messageList.setItemClickListener(new MessageListItemClickListener() {
			@Override
			public void onResendClick(EMMessage message) {
				new FEMaterialDialog.Builder(getActivity())
						.setMessage(R.string.confirm_resend)
						.setPositiveButton(R.string.resend, dialog -> resendMessage(message))
						.setNegativeButton(null, null)
						.setCancelable(true)
						.build().show();
			}

			@Override
			public void onBubbleLongClick(EMMessage message) {
				contextMenuMessage = message;
				new ChatContextFragmentDialog()
						.setEMMessage(message)
						.setOperstionListener(result -> operations(result))
						.show(getActivity().getSupportFragmentManager(), "ChatContextFragmentDialog");
			}

			@Override
			public void onUserAvatarClick(String username) {
				if (!TextUtils.isEmpty(username)) {
					String actualUserId = CoreZygote.getAddressBookServices().getActualUserId(username);
					FRouter.build(getActivity(), "/addressBook/detail").withString("user_id", actualUserId).go();
				}
			}

			@Override
			public void onUserAvatarLongClick(String username) {
				CoreZygote.getAddressBookServices().queryUserDetail(username)
						.subscribe(addressBook -> inputAtUsername(addressBook == null ? username : addressBook.name)
						, error -> inputAtUsername(username));
			}

			@Override
			public void onExtendMessageClick(String moduleId, Message message) {
				doExtendMessageClickEvent(moduleId, message);
			}

		});
	}

	@Override
	protected void registerExtendMenuItem() {
		super.registerExtendMenuItem();
		mInputMenu.registerExtendMenuItem(CommonUtil.getString(R.string.attach_video), R.drawable.em_chat_video_selector, ITEM_VIDEO);
		mInputMenu.registerExtendMenuItem(CommonUtil.getString(R.string.attach_file), R.drawable.em_chat_file_selector, ITEM_FILE);
		if (chatType == EaseUiK.EmChatContent.em_chatType_single) {
			mInputMenu.registerExtendMenuItem(CommonUtil.getString(R.string.attach_voice_call), R.drawable.em_chat_voice_call_selector,
					ITEM_VOICE_CALL);
			mInputMenu.registerExtendMenuItem(CommonUtil.getString(R.string.attach_video_call), R.drawable.em_chat_video_call_selector,
					ITEM_VIDEO_CALL);
		}
		mInputMenu.registerExtendMenuItem(getString(R.string.collaboration), R.drawable.em_chat_collaboration_selector, ITEM_COLLABORATION);
		mInputMenu.registerExtendMenuItem(getString(R.string.flow), R.drawable.em_chat_flow_selector, ITEM_FLOW);

		if (CoreZygote.getLoginUserServices() == null) return;

		if (CoreZygote.getLoginUserServices().hasModuleExist(14)) {
			mInputMenu.registerExtendMenuItem(getString(R.string.workplan), R.drawable.em_chat_plan_selector, ITEM_PLAN);
		}

		if (CoreZygote.getLoginUserServices().hasModuleExist(46)) {
			mInputMenu.registerExtendMenuItem(getString(R.string.email), R.drawable.em_chat_email_selector, ITEM_EMAIL);
		}

		if (CoreZygote.getLoginUserServices().hasModuleExist(37)) {
			mInputMenu.registerExtendMenuItem(getString(R.string.schedule), R.drawable.em_chat_schedule_selector, ITEM_SCHEDULE);
		}
	}

	protected void onExtendMenuItemClick(int itemId) {
		super.onExtendMenuItemClick(itemId);
		switch (itemId) {
			case ITEM_VIDEO:                                                    // 录视频
			case ITEM_VOICE_CALL:                                               // 语音聊天
			case ITEM_VIDEO_CALL:                                               // 视屏聊天
				FePermissions.with(this)
						.permissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
						.rationaleMessage(getResources().getString(R.string.permission_rationale_camera))
						.requestCode(itemId)
						.request();
				break;
			case ITEM_FILE:                                                     // 选择文件
				FRouter.build(getActivity(), "/media/file/select")
						.withStrings("extra_except_path", new String[]{CoreZygote.getPathServices().getUserPath()})
						.requestCode(REQUEST_CODE_SELECT_FILE).go();
				break;
			case ITEM_COLLABORATION:                                            // 发协同
				createNew("/collaboration/create", ITEM_COLLABORATION);
				break;
			case ITEM_EMAIL:                                                    // 发邮件
				createNew("/mail/create", ITEM_EMAIL);
				break;
			case ITEM_FLOW:                                                     // 发流程
//				FRouter.build(getActivity(), "/flow/list")
//						.withBool("fromIM", true).go();
				Module module = FunctionManager.findModule(Func.NewForm);
				if (module != null && !TextUtils.isEmpty(module.url)) {
					FRouter.build(getActivity(), "/x5/browser")
							.withString("appointURL", module.url)
							.withInt("moduleId", Func.Default)
							.withBool("isNewForm", true)
							.go();
				}
				break;
			case ITEM_PLAN:                                                     // 发计划
				createNew("/plan/create", ITEM_PLAN);
				break;
			case ITEM_SCHEDULE:                                                 // 发日程, 兼容老旧垃圾版本
				createNew("/x5/browser", ITEM_SCHEDULE, "moduleId", Func.Schedule);
			default:
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == android.app.Activity.RESULT_OK) {
			switch (requestCode) {
				case MediaRecorder.REQUEST_CODE_RECORD_VIDEO://send video
					sendVideoData(data);
					break;
				case REQUEST_CODE_SELECT_FILE: //send the file
					if (data != null) {
						ArrayList<String> selectedFiles = data.getStringArrayListExtra("SelectionData");
						if (CommonUtil.nonEmptyList(selectedFiles)) {
							boolean hasShow = false;
							for (String selectedFile : selectedFiles) {
								if (checkFileSize(selectedFile)) {
									sendFileMessage(selectedFile);
								}
								else if (!hasShow) {
									FEToast.showMessage(getString(R.string.em_sendfile_maxsize));
									hasShow = true;
								}
							}
						}
					}
					break;
				case REQUEST_CODE_SELECT_AT_USER:
					if (data != null) {
						String username = data.getStringExtra("username");
						inputAtUsername(username);
					}
					break;
				default:
					break;
			}
		}
	}

	private boolean checkFileSize(String path) {
		File file = new File(path);
		return !(file.exists() && file.length() >= FILE_MAX_SIZE);
	}


	private void operations(int result) {
		switch (result) {
			case ChatContextFragmentDialog.RESULT_CODE_COPY: // copy
				clipboard.setPrimaryClip(ClipData.newPlainText(null,
						((EMTextMessageBody) contextMenuMessage.getBody()).getMessage()));
				break;
			case ChatContextFragmentDialog.RESULT_CODE_DELETE: // delete
				conversation.removeMessage(contextMenuMessage.getMsgId());
				messageList.refresh();
				break;
			case ChatContextFragmentDialog.RESULT_CODE_FORWARD: // forward
				FRouter.build(getActivity(), "/im/forward")
						.withString("forward_msg_id", contextMenuMessage.getMsgId())
						.go();
				break;
			case ChatContextFragmentDialog.RESULT_CODE_RECALL: // 撤回消息
				if (java.lang.System.currentTimeMillis() - contextMenuMessage.getMsgTime() > RECALL_INTERVAL_TIME) {
					FEToast.showMessage(R.string.recall_time_error);
				}
				else {
					sendRecallCmdMsg(contextMenuMessage);
				}
				break;
			case ChatContextFragmentDialog.RESULT_CODE_REPLY:
				inputReplyMsg();
				break;
			case ChatContextFragmentDialog.RESULT_CODE_REMIND: {
				remindMessage();
			}
			case ChatContextFragmentDialog.RESULT_CODE_SPEKER: {
				MessageSettingManager manager = new MessageSettingManager();
				MessageSetting messageSetting = manager.query(EMClient.getInstance().getCurrentUser());
				messageSetting.speakerOn = !messageSetting.speakerOn;
				manager.update(messageSetting);
				if (TextUtils.isEmpty(messageSetting.userId)) {
					messageSetting.userId = EMClient.getInstance().getCurrentUser();
					manager.insert(messageSetting);
				}
				else {
					manager.update(messageSetting);
				}
			}
			default:
				break;
		}
		hideExtendMenu();
	}


	private void remindMessage() {
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_calendar))
				.requestCode(PermissionCode.CALENDAR)
				.request();
	}

	/**
	 * make a voice call
	 */
	public void startVoiceCall() {
		if (!EMClient.getInstance().isConnected()) {
			Toast.makeText(getActivity(), R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
		}
		else {
			startActivity(new Intent(getActivity(), VoiceCallActivity.class).putExtra("username", toChatUsername)
					.putExtra("isComingCall", false));
			mInputMenu.hideMenuContainer();
		}
	}

	/**
	 * make a video call
	 */
	public void startVideoCall() {
		if (!EMClient.getInstance().isConnected()) {
			Toast.makeText(getActivity(), R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
		}
		else {
			startActivity(new Intent(getActivity(), VideoCallActivity.class).putExtra("username", toChatUsername)
					.putExtra("isComingCall", false));
			mInputMenu.hideMenuContainer();
		}
	}

	public void createNew(String route, int requestCode) {
		createNew(route, requestCode, null, 0);
	}

	public void createNew(String route, int requestCode, String key, int moduleId) {
		if (chatType == EaseUiK.EmChatContent.em_chatType_single) {
			ArrayList<String> userIds = new ArrayList<>();
			userIds.add(toChatUsername);
			RouteCreator routeCreator = FRouter.build(getActivity(), route)
					.withInt("fromType", 100)
					.withStringArray("userIds", userIds)
					.requestCode(requestCode);
			if (!TextUtils.isEmpty(key)) {
				routeCreator.withInt(key, moduleId);
			}
			routeCreator.go();
		}
		else if (chatType == EaseUiK.EmChatContent.em_chatType_group) {
			EMGroup group = EMClient.getInstance().groupManager().getGroup(toChatUsername);
			if (group != null && CommonUtil.nonEmptyList(group.getMembers())) {
				ArrayList<String> userIds = new ArrayList<>();
				userIds.add(group.getOwner());
				List<String> members = group.getMembers();
				userIds.addAll(members);
				userIds.remove(EMClient.getInstance().getCurrentUser());
				RouteCreator routeCreator = FRouter.build(getActivity(), route)
						.withInt("fromType", 100)
						.withStringArray("userIds", userIds);
				if (!TextUtils.isEmpty(key)) {
					routeCreator.withInt(key, moduleId);
				}
				routeCreator.go();
				return;
			}

			FELoadingDialog loadingDialog = new FELoadingDialog.Builder(getActivity())
					.setLoadingLabel(getResources().getString(cn.flyrise.feep.core.R.string.core_loading_wait))
					.setCancelable(false)
					.create();
			loadingDialog.show();
			Observable
					.create((Subscriber<? super List<String>> f) -> {
						try {
							EMGroup emGroup = EMClient.getInstance().groupManager().getGroupFromServer(toChatUsername, true);
							List<String> userIds = new ArrayList<>();
							userIds.add(emGroup.getOwner());
							userIds.addAll(emGroup.getMembers());
							f.onNext(userIds);
						} catch (Exception e) {
							e.printStackTrace();
							f.onError(e);
						} finally {
							f.onCompleted();
						}
					})
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(results -> {
						loadingDialog.hide();
						ArrayList<String> userIds = new ArrayList<>(results);
						RouteCreator routeCreator = FRouter.build(getActivity(), route)
								.withInt("fromType", 100)
								.withStringArray("userIds", userIds);

						if (!TextUtils.isEmpty(key)) {
							routeCreator.withInt(key, moduleId);
						}

						routeCreator.go();
					}, exception -> {
						loadingDialog.hide();
						FRouter.build(getActivity(), route).go();
					});
		}
	}

	@PermissionGranted(ITEM_VIDEO)
	public void onVideoPermissionGranted() {
		startActivityForResult(new Intent(getActivity(), CameraActivity.class), MediaRecorder.REQUEST_CODE_RECORD_VIDEO);
	}

	@PermissionGranted(ITEM_VIDEO_CALL)
	public void onVideoCallPermissionGranted() {
		startVideoCall();
		messageList.refreshSelectLast();
	}

	@PermissionGranted(ITEM_VOICE_CALL)
	public void onVoiceCallPermissionGranted() {
		startVoiceCall();
		messageList.refreshSelectLast();
	}

	@PermissionGranted(PermissionCode.CALENDAR)
	public void onCalendarPermissionGanted() {
		openCalenderDialog();
	}


	private void openCalenderDialog() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 30);
		calendar.set(Calendar.MILLISECOND, 0);
		DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog();
		dateTimePickerDialog.setDateTime(calendar);
		dateTimePickerDialog.setButtonCallBack(new DateTimePickerDialog.ButtonCallBack() {
			@Override
			public void onClearClick() {
			}

			@Override
			public void onOkClick(Calendar calendar, DateTimePickerDialog dateTimePickerDialog) {
				calendar.set(Calendar.SECOND, 0);
				if (calendar.getTimeInMillis() < java.lang.System.currentTimeMillis() + 5 * 60 * 1000) {
					FEToast.showMessage(getString(R.string.schedule_remind_time_hint));
					return;
				}
				syncCalendarToSystem(calendar, contextMenuMessage);
				dateTimePickerDialog.dismiss();
			}
		});
		dateTimePickerDialog.setTimeLevel(DateTimePickerDialog.TIME_LEVEL_MIN);
		dateTimePickerDialog.show(getActivity().getFragmentManager(), "dateTimePickerDialog");
	}

	private void syncCalendarToSystem(Calendar calendar, EMMessage message) {
		showLoading();
		String remindText = EaseCommonUtils.isExtendMessage(message) ? getExtendMsgContent()
				: EmojiUtil.parseEmojiText(((EMTextMessageBody) message.getBody()).getMessage());
		Observable<Integer> observable = SystemScheduleUtil.addToSystemCalendar(getActivity(), remindText, remindText, calendar);
		observable.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(resultCode -> {
							hideLoading();
							if (resultCode == 200) {
								FEToast.showMessage(getString(R.string.schedule_remind_success));
							}
							else {
								FEToast.showMessage(getString(R.string.schedule_remind_error));
							}
						},
						exception -> {
							hideLoading();
							FEToast.showMessage(getString(R.string.schedule_remind_error));
						});
	}

	private void sendVideoData(Intent data) {
		if (data == null) return;
		String videoPath = data.getStringExtra("record_video");
		File file = new File(PathUtil.getInstance().getImagePath(), "thvideo" + java.lang.System.currentTimeMillis());
		try {
			FileOutputStream fos = new FileOutputStream(file);
			Bitmap ThumbBitmap = ThumbnailUtils.createVideoThumbnail(videoPath, 3);
			ThumbBitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.close();
			sendVideoMessage(videoPath, file.getAbsolutePath(), MediaRecorder.getRecorderSize(getActivity(), videoPath));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void inputAtUsername(String username) {
		inputAtUsername(username, true);
	}


	public void hideExtendMenu() {
		mInputMenu.hideMenuContainer();
	}

	private void doExtendMessageClickEvent(String moduleId, Message message) {
		if (TextUtils.isEmpty(moduleId) || TextUtils.equals(moduleId, "-1")) {
			String contentText = message.getSendTime() + message.getAction()
					+ "\n" + message.getTitle() + " " + message.getContent();
			String titleText = getActivity().getString(R.string.phone_does_not_support_message);
			new FEMaterialDialog.Builder(getActivity())
					.setTitle(titleText)
					.setMessage(contentText)
					.setPositiveButton(null, null)
					.build()
					.show();
		}
		else {
			int moduleType = CommonUtil.parseInt(moduleId);
			if (moduleType != -99) {
				onExtendMessageClick(moduleType, message);
			}
		}
		final List<String> ids = new ArrayList<>();
		final NoticesManageRequest reqContent = new NoticesManageRequest();
		ids.add(message.getMessageID());
		reqContent.setMsgIds(ids);
		reqContent.setUserId(CoreZygote.getLoginUserServices().getUserId());
		FEHttpClient.getInstance().post(reqContent, null);
	}

	private void onExtendMessageClick(int itemType, Message message) {
		switch (itemType) {
			case ToDo:    // 协同 待办
			case Done:    // 协同 已办
			case Trace:   // 协同 跟踪
			case ToSend:  // 协同 待发
			case Sended:  // 协同 已发
				FRouter.build(getActivity(), "/particular/detail")
						.withInt("extra_particular_type", 4)
						.withString("extra_business_id", message.getBusinessID())
						.withString("extra_message_id", message.getMessageID())
						.withInt("extra_request_type", itemType)
						.go();
				break;
			case News:
			case Announcement:
				FRouter.build(getActivity(), "/particular/detail")
						.withInt("extra_particular_type", itemType == 5 ? 1 : 2)
						.withString("extra_business_id", message.getBusinessID())
						.withString("extra_message_id", message.getMessageID())
						.withInt("extra_request_type", itemType)
						.go();
				break;
			case Meeting: // 会议
				if (FunctionManager.hasPatch(28)) {
					FRouter.build(getActivity(), "/meeting/detail")
							.withString("meetingId", message.getBusinessID())
							.go();
				}
				else {
					FRouter.build(getActivity(), "/particular/detail")
							.withInt("extra_particular_type", 3)
							.withString("extra_business_id", message.getBusinessID())
							.withString("extra_message_id", message.getMessageID())
							.go();
				}
				break;
			case Plan: // 计划
				if (FunctionManager.hasPatch(27)) {
					FRouter.build(getActivity(), "/plan/detail")
							.withString("EXTRA_BUSINESSID", message.getBusinessID())
							.withString("EXTRA_MESSAGEID", message.getMessageID())
							.go();
				}
				else {
					FRouter.build(getActivity(), "/particular/detail")
							.withInt("extra_particular_type", 5)
							.withString("extra_business_id", message.getBusinessID())
							.withString("extra_message_id", message.getMessageID())
							.go();
				}
				break;
			case InBox:// 邮件收件箱
				if (TextUtils.equals(message.getBusinessID(), "0")) {
					FRouter.build(getActivity(), "/mail/home")
							.withString("extra_type", getString(R.string.mail_box))
							.withString("extra_box_name", "InBox/Inner")
							.go();
				}
				else {
					FRouter.build(getActivity(), "/mail/detail")
							.withString("extra_mail_id", message.getBusinessID())
							.withString("extra_box_name", "InBox/Inner")
							.go();
				}
				break;
			case Knowledge:   // 文档
				FRouter.build(getActivity(), "/x5/browser")
						.withString("businessId", message.getBusinessID())
						.withString("messageId", message.getMessageID())
						.withInt("moduleId", Knowledge)
						.go();
				break;
			case Activity:    // 活动
				FRouter.build(getActivity(), "/x5/browser")
						.withString("businessId", message.getBusinessID())
						.withString("messageId", message.getMessageID())
						.withInt("moduleId", Activity)
						.go();
				break;
			case Vote:        // 投票
				FRouter.build(getActivity(), "/x5/browser")
						.withString("businessId", message.getBusinessID())
						.withString("messageId", message.getMessageID())
						.withInt("moduleId", Vote)
						.go();
				break;
			case Schedule:    // 日程
				FRouter.build(getActivity(), "/x5/browser")
						.withString("businessId", message.getBusinessID())
						.withString("messageId", message.getMessageID())
						.withInt("moduleId", Schedule)
						.go();
				break;
			case CircleNotice:
			case CRM:
				if (TextUtils.isEmpty(message.getUrl()) || TextUtils.isEmpty(message.getMessageID()) || "0"
						.equals(message.getBusinessID())) {
					StringBuilder builder = new StringBuilder();
					builder.append(message.getSendTime())
							.append(" ").append(message.getAction())
							.append("\n").append(message.getTitle());
					String content = message.getContent();
					if (!TextUtils.isEmpty(content)
							&& !TextUtils.equals(content, "null")
							&& !TextUtils.equals(content, message.getTitle())) {
						builder.append(" ").append(message.getContent());
					}

					String titleText = "暂不支持该类型消息";
					new FEMaterialDialog.Builder(getActivity())
							.setTitle(titleText)
							.setMessage(builder.toString())
							.setPositiveButton(null, null)
							.build()
							.show();
				}
				else {
					FRouter.build(getActivity(), "/x5/browser")
							.withString("appointURL", message.getUrl())
							.withInt("moduleId", Func.Default)
							.go();
				}
			default:
				break;
		}
	}

	private void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(getActivity())
				.setLoadingLabel(getResources().getString(R.string.core_loading_wait))
				.setCancelable(false)
				.create();
		mLoadingDialog.show();
	}

	private void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

}
