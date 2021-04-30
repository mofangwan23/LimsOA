package com.hyphenate.easeui.widget.chatrow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.ILoginUserServices;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.Direct;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import com.hyphenate.easeui.model.SendEMCallBack;
import com.hyphenate.easeui.utils.DateUtils;
import com.hyphenate.easeui.widget.EaseChatMessageList.MessageListItemClickListener;

public abstract class EaseChatRow extends RecyclerView.ViewHolder {

	protected static final String TAG = EaseChatRow.class.getSimpleName();

	protected LayoutInflater inflater;
	protected ProgressBar progressBar;
	private ImageView mIvAvatar;
	ImageView statusView;
	View bubbleLayout;

	protected TextView mTvTime;
	protected TextView tvState;
	private TextView mTvNick;
	private TextView tvAcked;
	private TextView deliveredView;
	TextView tvPercent;

	protected Activity activity;

	EMMessage preMessage;
	protected EMMessage message;
	protected MessageListItemClickListener itemClickListener;

	public EaseChatRow(View itemView) {
		super(itemView);
		findBaseView();
	}

	private void findBaseView() {
		mTvTime = itemView.findViewById(R.id.timestamp);
		mIvAvatar = itemView.findViewById(R.id.iv_userhead);
		bubbleLayout = itemView.findViewById(R.id.bubble);
		mTvNick = itemView.findViewById(R.id.tv_userid);
		progressBar = itemView.findViewById(R.id.progress_bar);
		statusView = itemView.findViewById(R.id.msg_status);
		tvAcked = itemView.findViewById(R.id.tv_ack);
		deliveredView = itemView.findViewById(R.id.tv_delivered);
		tvState = itemView.findViewById(R.id.tvState);
		tvPercent = itemView.findViewById(R.id.percentage);
		findView();
	}


	public void setView(Context context, EMMessage message, EMMessage preMessage, MessageListItemClickListener itemClickListener) {
		this.activity = (Activity) context;
		this.message = message;
		this.preMessage = preMessage;
		this.itemClickListener = itemClickListener;
		setUpBaseView();
		setClickListener();
	}

	void setUpBaseView() {
		// set nickname, avatar and background of bubble
		if (preMessage == null) {
			mTvTime.setText(DateUtil.formatTimeForDetail(message.getMsgTime()));
			mTvTime.setVisibility(View.VISIBLE);
		}
		else {
			if (DateUtils.isCloseEnough(message.getMsgTime(), preMessage.getMsgTime())) {
				mTvTime.setVisibility(View.GONE);
			}
			else {
				mTvTime.setText(DateUtil.formatTimeForDetail(message.getMsgTime()));
				mTvTime.setVisibility(View.VISIBLE);
			}
		}

		String mUserId = getCurrUserId();

		CoreZygote.getAddressBookServices().queryUserDetail(mUserId)
				.subscribe(addressBook -> {
					if (addressBook != null) {
						if (mIvAvatar != null) {
							ILoginUserServices services = CoreZygote.getLoginUserServices();
							String host = services.getServerAddress() + addressBook.imageHref;
							FEImageLoader.load(activity, mIvAvatar, host, mUserId, addressBook.name);
						}
						if (mTvNick != null) {
							if (message.getChatType() == EMMessage.ChatType.GroupChat) {
								mTvNick.setVisibility(View.VISIBLE);
								mTvNick.setText(addressBook.name);
							}
							else {
								mTvNick.setVisibility(View.GONE);
							}
						}
					}
					else {
						if (mIvAvatar != null) FEImageLoader.load(activity, mIvAvatar, R.drawable.ease_default_avatar);
						if (mTvNick != null) mTvNick.setText(mUserId);
					}
				}, error -> {
					if (mIvAvatar != null) FEImageLoader.load(activity, mIvAvatar, R.drawable.ease_default_avatar);
					if (mTvNick != null) mTvNick.setText(mUserId);
				});

		if (tvAcked != null) {
			if (message.isAcked()) {
				if (deliveredView != null) {
					deliveredView.setVisibility(View.GONE);
				}
				tvAcked.setVisibility(View.VISIBLE);
			}
			else {
				tvAcked.setVisibility(View.GONE);
			}
		}

		if (tvState != null) {
			if (message.getBooleanAttribute(EmChatContent.MESSAGE_ATTR_IS_REJECTION, false)) {
				tvState.setVisibility(View.VISIBLE);
				tvState.setText("消息已发送，但对方拒收了");
			}
			else {
				tvState.setVisibility(View.GONE);
			}
		}

		setUpView();

	}

	private String getCurrUserId() {
		String mUserId = "";
		if (message.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_COPY_GROUP, false)) {//是否为拷贝的聊天
			mUserId = message.getStringAttribute(EmChatContent.MESSAGE_COPY_GROUP_USER_ID, "");
		}

		if (TextUtils.isEmpty(mUserId)) {
			mUserId = message.direct() == Direct.SEND ? EMClient.getInstance().getCurrentUser() : message.getFrom();
		}
		return mUserId;
	}

	/**
	 * set callback for sending message
	 */
	void setMsgCallBack() {
		SendEMCallBack messageSendCallback = new SendEMCallBack(message, new EMCallBack() {
			@Override
			public void onSuccess() {
				updateView(0);
			}

			@SuppressLint("SetTextI18n") @Override
			public void onProgress(final int progress, String status) {
				activity.runOnUiThread(() -> {
					if (tvPercent != null) {
						tvPercent.setText(progress + "%");
					}
				});
			}

			@Override
			public void onError(int code, String error) {
				updateView(code);
			}
		});
		message.setMessageStatusCallback(messageSendCallback);
	}


	protected void setClickListener() {
		if (bubbleLayout != null) {
			bubbleLayout.setOnClickListener(v -> onBubbleClick());

			bubbleLayout.setOnLongClickListener(v -> {
				if (itemClickListener != null) {
					itemClickListener.onBubbleLongClick(message);
				}
				return true;
			});
		}

		if (statusView != null) {
			statusView.setOnClickListener(v -> {
				if (itemClickListener != null) {
					itemClickListener.onResendClick(message);
				}
			});
		}

		if (mIvAvatar != null) {
			mIvAvatar.setOnClickListener(v -> {
				if (itemClickListener != null) {
					if (message.direct() == Direct.SEND) {
						itemClickListener.onUserAvatarClick(EMClient.getInstance().getCurrentUser());
					}
					else {
						itemClickListener.onUserAvatarClick(message.getFrom());
					}
				}
			});

			mIvAvatar.setOnLongClickListener(v -> {
				if (itemClickListener != null) {
					if (message.direct() == Direct.SEND) {
						itemClickListener.onUserAvatarLongClick(EMClient.getInstance().getCurrentUser());
					}
					else {
						itemClickListener.onUserAvatarLongClick(message.getFrom());
					}
					return true;
				}
				return false;
			});
		}
	}

	private void updateView(final int errorCode) {
		activity.runOnUiThread(() -> {
			if (message.status() == EMMessage.Status.FAIL) {
				if (errorCode == EMError.MESSAGE_INCLUDE_ILLEGAL_CONTENT) {
					Toast.makeText(activity,
							activity.getString(R.string.send_fail) + activity.getString(R.string.error_send_invalid_content),
							Toast.LENGTH_SHORT).show();
				}
				else if (errorCode == EMError.GROUP_NOT_JOINED) {
					Toast.makeText(activity,
							activity.getString(R.string.send_fail) + activity.getString(R.string.error_send_not_in_the_group),
							Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(activity, activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast),
							Toast.LENGTH_SHORT).show();
				}
			}
			setUpBaseView();
		});
	}

	/**
	 * find view by id
	 */
	protected abstract void findView();

	/**
	 * setup view
	 */
	protected abstract void setUpView();

	/**
	 * on bubble clicked
	 */
	protected abstract void onBubbleClick();

}
