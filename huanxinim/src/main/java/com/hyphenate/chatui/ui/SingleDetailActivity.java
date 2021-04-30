package com.hyphenate.chatui.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.IConvSTService;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.squirtlez.frouter.FRouter;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chat.EMGroupOptions;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.utils.MMPMessageUtil;
import com.hyphenate.exceptions.HyphenateException;
import java.util.Collections;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP 单聊详情
 * @since 2017-03-16 15:07
 */
@Route("/im/single/detail")
@RequestExtras("userId")
public class SingleDetailActivity extends BaseActivity {

	public static final int CODE_ADD_CONTACTS = 8088;   // 选择联系人
	private TextView mTvBlackList;
	private TextView mTvBlackListPrompt;
	private TextView mTvSearchRecord;

	private String mChatUserId;
	private boolean isBlackUserId;                      // 是否已经被加入了黑名单
	private FELoadingDialog mLoadingDialog;

	private ImageView mIvAvatar;
	private UISwitchButton mSilenceModeBtn;

	private UISwitchButton mBtTop;

	private AddressBook mAddressBook;
	private IConvSTService mCSTService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mChatUserId = getIntent().getStringExtra("userId");
		mCSTService = CoreZygote.getConvSTServices();
		setContentView(R.layout.em_activity_single_detail);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(getResources().getString(R.string.em_txt_conversation_setting));
	}

	@Override
	public void bindView() {
		mSilenceModeBtn = findViewById(R.id.switchSilence);
		mSilenceModeBtn.setChecked(mCSTService != null && mCSTService.isSilence(mChatUserId));

		mBtTop = findViewById(R.id.btTop);

		mTvBlackList = findViewById(R.id.tvAddBlackList);
		mTvBlackListPrompt = findViewById(R.id.tvBlackListPrompt);
		mTvSearchRecord = findViewById(R.id.tvSearchChatRecord);

		mIvAvatar = findViewById(R.id.ivAvatar);
		CoreZygote.getAddressBookServices().queryUserDetail(mChatUserId)
				.subscribe(addressBook -> {
					mAddressBook = addressBook;
					if (mAddressBook != null) {
						String host = CoreZygote.getLoginUserServices().getServerAddress();
						FEImageLoader.load(this, mIvAvatar, host + mAddressBook.imageHref, mAddressBook.userId, mAddressBook.name);
					}
					else {
						FEImageLoader.load(this, mIvAvatar, R.drawable.ease_default_avatar);
					}
				}, error -> {
					FEImageLoader.load(this, mIvAvatar, R.drawable.ease_default_avatar);
				});
		changeBlackListState(false);
		Observable
				.unsafeCreate(f -> {
					try {
						List<String> blackList = EMClient.getInstance().contactManager().getBlackListFromServer();
						f.onNext(!CommonUtil.isEmptyList(blackList) && (blackList.contains(mChatUserId)));
					} catch (HyphenateException e) {
						e.printStackTrace();
						f.onNext(false);
					}
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(f -> {
					isBlackUserId = (boolean) f;
					changeBlackListState(isBlackUserId);
				});
		EMConversation conversation = EMClient.getInstance().chatManager().getConversation(mChatUserId);
		mBtTop.setChecked(!TextUtils.isEmpty(conversation.getExtField()));
	}

	@Override
	public void bindListener() {
		mSilenceModeBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (mCSTService != null) {
				String conversation = mAddressBook != null ? mAddressBook.name : "";
				if (isChecked) {
					mCSTService.makeConversationSilence(mChatUserId, conversation);
				}
				else {
					mCSTService.makeConversationActive(mChatUserId, conversation);
				}
			}
		});

		mIvAvatar.setOnClickListener(v -> FRouter.build(SingleDetailActivity.this, "/addressBook/detail")
				.withString("user_id", mChatUserId)
				.go());

		findViewById(R.id.ivAddUser).setOnClickListener(v -> {                  // 添加联系人
			List<AddressBook> addressBooks = CoreZygote.getAddressBookServices().queryUserIds(Collections.singletonList(mChatUserId));
			//再把登录用户给加进去。
			addressBooks.add(CoreZygote.getAddressBookServices().queryUserInfo(CoreZygote.getLoginUserServices().getUserId()));
			DataKeeper.getInstance().keepDatas(SingleDetailActivity.this.hashCode(), addressBooks);
			FRouter.build(SingleDetailActivity.this, "/addressBook/list")
					.withBool("select_mode", true)
//                    .withBool("except_selected", true)
					.withInt("data_keep", SingleDetailActivity.this.hashCode())
					.withString("address_title", getString(R.string.em_title_select_contact))
					.requestCode(CODE_ADD_CONTACTS)
					.go();
		});

		mTvBlackList.setOnClickListener(v -> {
			if (isBlackUserId) {                                    // 移出黑名单
				try {
					EMClient.getInstance().contactManager().removeUserFromBlackList(mChatUserId);
					changeBlackListState(isBlackUserId = false);
				} catch (HyphenateException e) {
					e.printStackTrace();
					FEToast.showMessage(getString(R.string.em_txt_remove_black_failed));
				}
			}
			else {                                                  // 添加黑名单
				new FEMaterialDialog.Builder(SingleDetailActivity.this)
						.setMessage(R.string.em_txt_add_black_prompt)
						.setPositiveButton(null, dialog -> {
							try {
								EMClient.getInstance().contactManager().addUserToBlackList(mChatUserId, false);
								changeBlackListState(isBlackUserId = true);
							} catch (HyphenateException e) {
								e.printStackTrace();
								FEToast.showMessage(getString(R.string.em_txt_add_black_failed));
							}
						})
						.setNegativeButton(getResources().getString(R.string.core_btn_negative), null)
						.build()
						.show();
			}
		});

		findViewById(R.id.tvClearChatRecord).setOnClickListener(v -> {          // 清除聊天记录
			new FEMaterialDialog.Builder(SingleDetailActivity.this)
					.setMessage(getResources().getString(R.string.Whether_to_empty_all_chats))
					.setNegativeButton(null, null)
					.setPositiveButton(null, dialog -> {
						showLoading();
						Observable
								.create((Subscriber<? super Boolean> f) -> {
									EMConversation conversation = EMClient.getInstance().chatManager().getConversation(mChatUserId);
									EMMessage lastMessage = conversation.getLastMessage();
									conversation.clearAllMessages();
									if (lastMessage != null) {
										MMPMessageUtil.saveClearHistoryMsg(mChatUserId, false, lastMessage.getMsgTime());
									}
									f.onNext(true);
								})
								.subscribeOn(Schedulers.io())
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(result -> {
									hideLoading();
									FEToast.showMessage(result ? getString(R.string.em_txt_chat_record_delete_success)
											: getString(R.string.em_txt_chat_record_delete_failed));
								}, exception -> {
									hideLoading();
									FEToast.showMessage(getString(R.string.em_txt_chat_record_delete_failed));
								});
						setResult(200);
					})
					.build()
					.show();
		});
		mTvSearchRecord.setOnClickListener(v -> startActivity(
				new Intent(SingleDetailActivity.this, ChatRecordSearchActivity.class).putExtra("conversationId", mChatUserId)));

		mBtTop.setOnCheckedChangeListener((buttonView, isChecked) -> {
			EMConversation conversation = EMClient.getInstance().chatManager().getConversation(mChatUserId);
			conversation.setExtField(isChecked ? "1" : "");
		});

	}

	private void changeBlackListState(boolean isBlackUserId) {
		mTvBlackList.setText(isBlackUserId ? getString(R.string.em_txt_add_to_black) : getString(R.string.em_txt_remove_from_black));
		mTvBlackListPrompt.setVisibility(isBlackUserId ? View.VISIBLE : View.GONE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CODE_ADD_CONTACTS && resultCode == 2048) {
			List<AddressBook> addressBooks = (List<AddressBook>) DataKeeper.getInstance()
					.getKeepDatas(SingleDetailActivity.this.hashCode());
			if (CommonUtil.nonEmptyList(addressBooks)) {
				CoreZygote.getAddressBookServices().queryUserDetail(mChatUserId)
						.subscribe(chatUser -> {
							boolean hasTheSame = false;
							for (AddressBook addressBook : addressBooks) {
								if (TextUtils.equals(addressBook.userId, chatUser.userId)) {
									hasTheSame = true;
									break;
								}
							}
							if (chatUser != null && !hasTheSame) {
								addressBooks.add(0, chatUser);
							}
							createNewGroup(addressBooks);
						}, error -> {

						});

			}
		}
	}

	private void createNewGroup(final List<AddressBook> addressBooks) {
		showLoading();
		Observable
				.create((Observable.OnSubscribe<EMGroup>) subscriber -> {
					StringBuilder groupName = new StringBuilder(CoreZygote.getLoginUserServices().getUserName());
					final String[] userIds = new String[addressBooks.size()];
					for (int i = 0, n = addressBooks.size(); i < n; i++) {
						userIds[i] = IMHuanXinHelper.getInstance().getImUserId(addressBooks.get(i).userId);
					}
					//把原聊天用户加进去
					int count = addressBooks.size() >= 3 ? 3 : addressBooks.size();
					for (int i = 0; i < count; i++) {
						if (!groupName.toString().contains(addressBooks.get(i).name)) {
							groupName.append("、").append(addressBooks.get(i).name);
						}
					}
					try {
						EMGroupOptions option = new EMGroupOptions();
						String reason = getResources().getString(R.string.invite_join_group);
						reason = EMClient.getInstance().getCurrentUser() + reason + groupName;
						option.maxUsers = 500;
						option.style = EMGroupManager.EMGroupStyle.EMGroupStylePrivateMemberCanInvite;
						EMGroup group = EMClient.getInstance().groupManager()
								.createGroup(groupName.toString(), "", userIds, reason, option);
						MMPMessageUtil.sendInviteMsg(group.getGroupId(), addressBooks);
						subscriber.onNext(group);
					} catch (Exception e) {
						e.printStackTrace();
						subscriber.onError(e);
					} finally {
						subscriber.onCompleted();
					}
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(group ->
				{
					hideLoading();
					Intent intent = new Intent(SingleDetailActivity.this, ChatActivity.class);
					intent.putExtra(EaseUiK.EmChatContent.emChatID, group.getGroupId());
					intent.putExtra(EaseUiK.EmChatContent.emChatType, EaseUiK.EmChatContent.em_chatType_group);
					startActivity(intent);
					finish();
				}, exception ->
				{
					FEToast.showMessage(getString(R.string.em_txt_create_group_failed));
					hideLoading();
					exception.printStackTrace();
				});
	}

	private void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(this)
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
