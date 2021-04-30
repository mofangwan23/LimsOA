package com.hyphenate.easeui.widget;

import static cn.flyrise.feep.core.common.utils.DevicesUtil.getKeyBoardHeight;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import cn.flyrise.feep.core.base.views.SwipeBackLayout;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.model.EaseDefaultEmojiconDatas;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.widget.EaseChatPrimaryMenuBase.EaseChatPrimaryMenuListener;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenu;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenuBase;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenuBase.EaseEmojiconMenuListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.greenrobot.eventbus.EventBus;


/**
 * input menu
 * including below component:
 * EaseChatPrimaryMenu: main menu bar, text input, send button
 * EaseChatExtendMenu: grid menu with image, file, location, etc
 * EaseEmojiconMenu: emoji icons
 */
public class EaseChatInputMenu extends LinearLayout {

	protected FrameLayout mPrimaryMenuLayout;
	protected EaseChatPrimaryMenuBase mPrimaryMenu;

	protected FrameLayout mEmojiconMenuLayout;
	protected EaseEmojiconMenuBase mEmojiconMenu;

	protected FrameLayout mExtendMenuLayout;
	protected NiuBiChatExtendMenu mExtendMenu;

	protected View mChatListView;
	protected LayoutInflater mLayoutInflater;


	private Handler mHandler = new Handler();
	private ChatInputMenuListener mChatInputListener;
	private Context mContext;
	private boolean isInitialized;

	//是否为语音状态
	private boolean isVoiceState = false;

	public EaseChatInputMenu(Context context) {
		this(context, null);
	}


	public EaseChatInputMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EaseChatInputMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		mLayoutInflater.inflate(R.layout.ease_widget_chat_input_menu, this);
		mPrimaryMenuLayout = findViewById(R.id.primary_menu_container);
		mEmojiconMenuLayout = findViewById(R.id.emojicon_menu_container);
		mExtendMenuLayout = findViewById(R.id.extend_menu_container);
		mExtendMenu = findViewById(R.id.chatExtendMenu);
	}

	public void setChatListView(View chatListView) {
		this.mChatListView = chatListView;
	}

	private void lockChatListView() {
		if (mChatListView == null) {
			return;
		}
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mChatListView.getLayoutParams();
		params.height = mChatListView.getHeight();
		params.weight = 0.0F;
	}

	private void unLockChatListView() {
		if (mChatListView == null) {
			return;
		}
		postDelayed(() -> ((LinearLayout.LayoutParams) mChatListView.getLayoutParams()).weight = 1.0F, 200L);
	}

	@SuppressLint("InflateParams")
	public void init(List<EaseEmojiconGroupEntity> emojiconGroupList) {
		if (isInitialized) {
			return;
		}

		// primary menu, use default if no customized one
		if (mPrimaryMenu == null) {
			mPrimaryMenu = (EaseChatPrimaryMenu) mLayoutInflater.inflate(R.layout.ease_layout_chat_primary_menu, null);
		}

		mPrimaryMenuLayout.addView(mPrimaryMenu);

		// emojicon menu, use default if no customized one
		if (mEmojiconMenu == null) {
			mEmojiconMenu = (EaseEmojiconMenu) mLayoutInflater.inflate(R.layout.ease_layout_emojicon_menu, null);
			if (emojiconGroupList == null) {
				emojiconGroupList = new ArrayList<>();
				emojiconGroupList.add(new EaseEmojiconGroupEntity(R.drawable.ee_1, Arrays.asList(EaseDefaultEmojiconDatas.getData())));
			}
			((EaseEmojiconMenu) mEmojiconMenu).init(emojiconGroupList);
		}

		mEmojiconMenuLayout.addView(mEmojiconMenu);
		processChatMenu();

		mExtendMenu.initialize();
		isInitialized = true;
	}

	public void setExtendMenuItemClickListener(NiuBiChatExtendMenu.OnMenuItemClickListener itemClickListener) {
		mExtendMenu.setOnExtendMenuListener(itemClickListener);
	}

	public EaseChatPrimaryMenuBase getPrimaryMenu() {
		return mPrimaryMenu;
	}

	public EaseEmojiconMenuBase getmEmojiconMenu() {
		return mEmojiconMenu;
	}

	public void registerExtendMenuItem(String name, int drawableRes, int itemId) {
		mExtendMenu.addMenu(itemId, drawableRes, name);
	}

	protected void processChatMenu() {
		mPrimaryMenu.setChatPrimaryMenuListener(new EaseChatPrimaryMenuListener() {

			@Override
			public void onSendBtnClicked(String content) {
				if (mChatInputListener != null) {
					mChatInputListener.onSendMessage(content);
				}
			}

			@Override
			public void onToggleVoiceBtnClicked() {
				hideMenuContainer();
			}

			@Override
			public void onToggleExtendClicked() {
				toggleMore();
			}

			@Override
			public void onToggleEmojiconClicked() {
				toggleEmojicon();
			}

			@Override
			public void onEditTextClicked() {
				hideMenuContainer();
			}

			@Override
			public void onKeyboardBtnClick() {
				mExtendMenuLayout.setVisibility(View.GONE);
			}


			@Override
			public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
				return mChatInputListener != null && mChatInputListener.onPressToSpeakBtnTouch(v, event);
			}
		});

		// emojicon menu
		mEmojiconMenu.setEmojiconMenuListener(new EaseEmojiconMenuListener() {

			@Override
			public void onExpressionClicked(EaseEmojicon emojicon) {
				if (emojicon.getType() != EaseEmojicon.Type.BIG_EXPRESSION) {
					if (emojicon.getEmojiText() != null) {
						mPrimaryMenu.onEmojiconInputEvent(EaseSmileUtils.getSmiledText(mContext, emojicon.getEmojiText()));
					}
				}
				else {
					if (mChatInputListener != null) {
						mChatInputListener.onBigExpressionClicked(emojicon);
					}
				}
			}

			@Override
			public void onDeleteImageClicked() {
				mPrimaryMenu.onEmojiconDeleteEvent();
			}
		});
	}

	/**
	 * insert text
	 */
	public void insertText(String text) {
		getPrimaryMenu().onTextInsert(text);
	}

	protected void toggleMore() {
		EaseChatPrimaryMenu chatPrimary = (EaseChatPrimaryMenu) mPrimaryMenu;
		if (mExtendMenuLayout.getVisibility() == View.GONE) {
			hideKeyboard();
			mHandler.postDelayed(() -> {
				resetExtendMenuHeight();
				hideEmojiMenu();
				chatPrimary.setEditTextBottomLine(false);
			}, 150);
		}
		else {
			if (mEmojiconMenu.getVisibility() == View.VISIBLE) {
				mEmojiconMenu.setVisibility(View.GONE);
				mExtendMenu.setVisibility(View.VISIBLE);
				chatPrimary.setEditTextBottomLine(false);
			}
			else {
				chatPrimary.setEditTextBottomLine(true);
				mExtendMenuLayout.setVisibility(View.GONE);
				DevicesUtil.showKeyboard(mPrimaryMenu.getEditText());
			}
		}

		if (chatPrimary.rlButtonSpeak != null && chatPrimary.faceChecked != null) {
			if (chatPrimary.rlButtonSpeak.getVisibility() == VISIBLE) {
				chatPrimary.faceChecked.setVisibility(VISIBLE);
				chatPrimary.rlButtonSpeak.setVisibility(GONE);
			}
		}
		EventBus.getDefault().post(SwipeBackLayout.DISABLE_SWIPE);
	}

	protected void toggleEmojicon() {
		EaseChatPrimaryMenu chatPrimary = (EaseChatPrimaryMenu) mPrimaryMenu;
		if (isVoiceState) {
			isVoiceState = false;
			chatPrimary.setModeKeyboard();
		}
		hideKeyboard();
		chatPrimary.setEditTextBottomLine(true);
		hideExtendMenu();
		mHandler.postDelayed(() -> {
			resetEmojoMenuHeight();
		}, 150);

		EventBus.getDefault().post(SwipeBackLayout.DISABLE_SWIPE);
	}

	private void resetExtendMenuHeight() {
		mExtendMenuLayout.getLayoutParams().height = calculateSoftInputHeight();
		mExtendMenu.setVisibility(View.VISIBLE);
		mExtendMenuLayout.setVisibility(View.VISIBLE);
	}

	private void resetEmojoMenuHeight() {
		mExtendMenuLayout.getLayoutParams().height = calculateSoftInputHeight();
		mEmojiconMenuLayout.setVisibility(View.VISIBLE);
		mEmojiconMenu.setVisibility(View.VISIBLE);
		mExtendMenuLayout.setVisibility(View.VISIBLE);
	}

	private int calculateSoftInputHeight() {
		int softInputHeight = DevicesUtil.getSupportSoftInputHeight((Activity) mContext);
		FELog.i("After call getSupportSoftInputHeight = " + softInputHeight);

		if (softInputHeight == 0) {
			softInputHeight = getKeyBoardHeight();
			FELog.i("softInputHeight = 0, call getKeyBoardHeight = " + softInputHeight);
			if (TextUtils.equals(Build.BOARD, "sdm660")
					&& TextUtils.equals(Build.MODEL, "OS105")
					&& TextUtils.equals(Build.DEVICE, "osborn")) {                // 辣鸡锤子手机
				softInputHeight = getKeyBoardHeight() + 196;
				FELog.i("Fucking chuizi, re-calculate softInputHeight = " + softInputHeight);
			}
		}
		else if (softInputHeight == -144) {
			softInputHeight = getKeyBoardHeight() + 144;
			FELog.i("Hidden virtual navigation bar, need add 144 height, softInputHeight = " + softInputHeight);
		}
		else {
			softInputHeight = getKeyBoardHeight() + 196;
			FELog.i("The fucking phone, need to add 196 height, softInputHeight = " + softInputHeight);
		}

		FELog.i("Final soft input height = " + softInputHeight);
		return softInputHeight;
	}

	public void setText(SpannableString text) {
		EditText editText = getPrimaryMenu().getEditText();
		editText.setText(text);
		editText.append("\n");
		editText.setSelection(editText.getText().length());
	}

	/**
	 * hide keyboard
	 */
	private void hideKeyboard() {
		mPrimaryMenu.hideKeyboard();
	}

	public void hideMenuContainer() {
		isVoiceState = true;
		mExtendMenu.setVisibility(View.GONE);
		mExtendMenuLayout.setVisibility(View.GONE);
		mEmojiconMenu.setVisibility(View.GONE);
		mEmojiconMenuLayout.setVisibility(View.GONE);
		mPrimaryMenu.onExtendMenuContainerHide();
		EventBus.getDefault().post(SwipeBackLayout.ENABLE_SWIPE);       // 允许左滑
	}

	private void hideEmojiMenu() {
		mEmojiconMenu.setVisibility(View.GONE);
		mEmojiconMenuLayout.setVisibility(View.GONE);
	}

	private void hideExtendMenu() {
		mExtendMenu.setVisibility(View.GONE);
	}

	/**
	 * when back key pressed
	 * @return false--extend menu is on, will hide it first true --extend menu is off
	 */
	public boolean onBackPressed() {
		if (mExtendMenuLayout.getVisibility() == View.VISIBLE) {
			hideMenuContainer();
			return false;
		}
		else {
			return true;
		}
	}


	public void setChatInputMenuListener(ChatInputMenuListener listener) {
		this.mChatInputListener = listener;
	}

	public interface ChatInputMenuListener {

		/**
		 * when send message button pressed
		 */
		void onSendMessage(String content);

		/**
		 * when big icon pressed
		 */
		void onBigExpressionClicked(EaseEmojicon emojicon);

		/**
		 * when speak button is touched
		 */
		boolean onPressToSpeakBtnTouch(View v, MotionEvent event);
	}

	private boolean isKeyboardShown() {
		return DevicesUtil.getSupportSoftInputHeight((Activity) mContext) != 0;
	}

}
