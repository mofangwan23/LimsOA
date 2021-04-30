package cn.flyrise.feep.particular;

import static cn.flyrise.feep.cordova.CordovaContract.CordovaPresenters.BRIDGE_BEFORE;
import static cn.flyrise.feep.cordova.CordovaContract.CordovaPresenters.BRIDGE_LAST;
import static cn.flyrise.feep.cordova.CordovaContract.CordovaPresenters.COLON_CODE;
import static cn.flyrise.feep.cordova.CordovaContract.CordovaPresenters.JAVASCRIPT;
import static cn.flyrise.feep.particular.presenter.ParticularPresenter.PARTICULAR_COLLABORATION;
import static cn.flyrise.feep.particular.presenter.ParticularPresenter.PARTICULAR_MEETING;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.library.view.BubbleWindow;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.android.protocol.model.Reply;
import cn.flyrise.android.protocol.model.SupplyContent;
import cn.flyrise.android.protocol.model.TrailContent;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.activity.AssociateCollaborationActivity;
import cn.flyrise.feep.collaboration.utility.DataStack;
import cn.flyrise.feep.commonality.manager.XunFeiVoiceInput;
import cn.flyrise.feep.commonality.view.TouchableWebView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.ListenableScrollView;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.flyrise.feep.form.been.ExecuteResult;
import cn.flyrise.feep.media.attachments.NetworkAttachmentListDialog;
import cn.flyrise.feep.media.attachments.NetworkAttachmentListFragment;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.common.AttachmentBeanConverter;
import cn.flyrise.feep.media.common.AttachmentUtils;
import cn.flyrise.feep.media.common.LuBan7;
import cn.flyrise.feep.news.bean.RelatedNews;
import cn.flyrise.feep.particular.presenter.CollaborationParticularPresenter;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;
import cn.flyrise.feep.particular.views.AnimationUtils;
import cn.flyrise.feep.particular.views.FEFloatingActionMenu;
import cn.flyrise.feep.particular.views.MeetingAttendView;
import cn.flyrise.feep.particular.views.ParticularContentView;
import cn.flyrise.feep.particular.views.ParticularHeadView;
import cn.flyrise.feep.particular.views.ParticularReplyEditView;
import cn.flyrise.feep.particular.views.RelativeElegantAdapter;
import cn.flyrise.feep.particular.views.RelativeElegantLayout;
import cn.flyrise.feep.particular.views.ReplyRelativeElegantAdapter;
import cn.flyrise.feep.utils.Patches;
import cn.squirtlez.frouter.FRouter;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-10-20 10:59
 */
@Route("/particular/detail")
@RequestExtras({"extra_particular_type", "extra_request_type", "extra_message_id", "extra_business_id"})
public class ParticularActivity extends NotTranslucentBarActivity implements ParticularContract.IView {

	private static final int CODE_OPEN_ATTACHMENT_PICKER = 1024;
	private static final int CODE_START_INTENT = 1025;
	public static final int CODE_OPEN_ADDBODY = 1026;
	public static final int CODE_SELECT_COLLECTION_FOLDER = 1027;

	private List<String> mLocalAttachments;
	private final Handler mHandler = new Handler();

	private WindowManager mWindowManager;
	private DataStack mDataStack;
	private BubbleWindow mBubbleWindow;
	private MeetingAttendView mMeetingAttendView;

	private View mOriginalReplyView;
	private View mReplyView;
	private View mAttachmentView;
	private ViewGroup mWaterMarkContentLayout;
	private ParticularReplyEditView mReplyEditView;

	private FEToolbar mToolBar;
	private FELoadingDialog mLoadingDialog;
	private ParticularContract.IPresenter mPresenter;

	private ViewGroup mBottomMenu;
	private ParticularHeadView mHeadView;
	private ParticularContentView mContentView;
	private ListenableScrollView mScrollView;
	private FEFloatingActionMenu mFloatingActionMenu;

	private long hindReplyVivewTime;

	private boolean isMettingReply = false;
	private XunFeiVoiceInput mVoiceInput;
	private String curReplyId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_particular);
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "");
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		this.mToolBar = toolbar;
	}

	@Override
	public void bindView() {
		mBottomMenu = findViewById(R.id.layoutBottomMenu);
		mHeadView = findViewById(R.id.particularHeadView);
		mContentView = findViewById(R.id.particularContentView);
		mScrollView = findViewById(R.id.nestedScrollView);
		mFloatingActionMenu = findViewById(R.id.feFloatingActionMenu);
		mWaterMarkContentLayout = findViewById(R.id.layoutCollaborationDetail);
	}

	@Override
	public void bindData() {
		mVoiceInput = new XunFeiVoiceInput(this);
		mDataStack = DataStack.getInstance();
		mPresenter = new ParticularPresenter.Builder(this)
				.setParticularView(this)
				.setStartIntent(getIntent())
				.build();
		if (mPresenter != null) {
			mPresenter.start();
		}
	}

	@Override
	public void bindListener() {
		mContentView.setWebViewWatcher(new ParticularContentView.WebViewWatcher() {
			@Override
			public void onWebPageStart() {
				mScrollView.scrollTo(0, 0);
			}

			@Override
			public void onWebPageFinished() {
				dismissLoading(null);
				if (mPresenter instanceof CollaborationParticularPresenter) {
					WMStamp.getInstance().draw(mWaterMarkContentLayout, mScrollView);
				}
			}

			@Override
			public boolean shouldUrlIntercept(String url, boolean isFeForm) {
				if (TextUtils.isEmpty(url)) {
					return true;
				}
				if (mPresenter == null) {
					return true;
				}
				int particularType = mPresenter.getParticularIntent().getParticularType();
				if (!isFeForm) {
					if (particularType == PARTICULAR_COLLABORATION || particularType == PARTICULAR_MEETING) {
						if (mAttachmentView != null && mAttachmentView.getVisibility() == View.VISIBLE) {
							mScrollView.smoothScrollTo(0, getViewHeight(mHeadView) + getViewHeight(mContentView));
							return true;
						}
					}
				}
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
				} catch (Exception exp) {
				}
				return false;
			}

			@Override
			public void onJsCallback(int jsControlInfo) {
				if (jsControlInfo != 12) {
					return;
				}
				if (mPresenter instanceof CollaborationParticularPresenter) {
					String queryString = ((CollaborationParticularPresenter) mPresenter).getQueryString();
					if (queryString == null) {
						return;
					}

					ExecuteResult info = new ExecuteResult();
					info.setActionType(X.JSActionType.PushData);
					info.setData(queryString);

					String javaScript = BRIDGE_BEFORE + info.getProperties() + BRIDGE_LAST;
					FELog.e(javaScript);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						mContentView.getWebView().evaluateJavascript(javaScript, FELog::i);
					}
					else {
						mContentView.getWebView().loadUrl(JAVASCRIPT + COLON_CODE + javaScript);
					}
				}
			}
		});

		mScrollView.addOnScrollChangeListener((scrollY, lastScrollY) -> {
			try {
				int height = mScrollView.getHeight();
				int measureHeight = mScrollView.getChildAt(0).getMeasuredHeight();
				boolean isScrollToBottom = (height + scrollY == measureHeight);
				if (mFloatingActionMenu != null) {
					mFloatingActionMenu.collapse();
				}
				if ((scrollY == 0 || lastScrollY - scrollY > 10) && mFloatingActionMenu.getVisibility() == View.GONE) {
					AnimationUtils.executeDisplayAnimation(mFloatingActionMenu);
				}
				else if ((isScrollToBottom || scrollY - lastScrollY >= 10) && mFloatingActionMenu.getVisibility() == View.VISIBLE) {
					AnimationUtils.executeHideAnimation(mFloatingActionMenu, mFloatingActionMenu.getHeight());
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		});

		mContentView.getWebView().setOnTouchEventListener(new TouchableWebView.onTouchEventListener() {
			private float lastY;

			@Override
			public boolean canIntercept(MotionEvent event) {
				float y = event.getY();
				if (y - lastY >= 0 && mContentView.getWebView().getScrollY() != 0 && mScrollView.getScrollY() <= mHeadView.getHeight()) {
					lastY = y;
					return true;
				}
				lastY = y;
				return false;
			}
		});

		mToolBar.setNavigationOnClickListener(v -> {
			if (System.currentTimeMillis() - hindReplyVivewTime > 100) {
				mPresenter.handleBackButton();
				finish();
			}
		});
		mVoiceInput.setOnRecognizerDialogListener(result -> {
			EditText editText = mReplyEditView.getReplyEditText();
			int selection = editText.getSelectionStart();
			XunFeiVoiceInput.setVoiceInputText(editText, result, selection);
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mPresenter != null) {
			mPresenter.handleBackButton();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		WMStamp.getInstance().clearWaterMark(this);
		SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "");
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
		if (mDataStack != null) {
			mDataStack = null;
		}
		if (mPresenter != null) {
			mPresenter = null;
		}
	}

	@Override
	public void showLoading() {
		if (mLoadingDialog == null) {
			mLoadingDialog = new FELoadingDialog
					.Builder(this)
					.setLoadingLabel(getResources().getString(R.string.core_loading))
					.setCancelable(true)
					.create();
		}
		mLoadingDialog.show();
	}

	@Override
	public void showLoadingWithProgress(int progress) {
		if (mLoadingDialog != null) {
			mLoadingDialog.updateProgress(progress);
		}
	}

	@Override
	public void dismissLoading(String text) {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
		if (!TextUtils.isEmpty(text)) {
			FEToast.showMessage(text);
		}
	}

	@Override
	public void setToolBarTitle(String title) {
		if (mToolBar == null) return;
		mToolBar.setTitle(title);
	}

	@Override
	public void configToolBarRightText(String rightText) {
		if (mPresenter == null || mToolBar == null) return;
		mToolBar.setRightText(rightText);
		mToolBar.setRightTextClickListener(mPresenter::toolBarRightTextClick);
	}

	/**
	 * 获取详情失败
	 * @param errorMessage 错误信息，可能为空
	 */
	@Override
	public void fetchDetailError(String errorMessage) {
		if (mBottomMenu == null) return;
		this.dismissLoading(null);
		mBottomMenu.setVisibility(View.GONE);
		mBottomMenu.setTag("NONE");
		mScrollView.setVisibility(View.GONE);
		mFloatingActionMenu.setVisibility(View.GONE);
		if (!TextUtils.isEmpty(errorMessage)) {
			TextView textView = (TextView) findViewById(R.id.tvErrorText);
			if (textView != null) {
				textView.setText(errorMessage);
			}
		}

		View errorPromptView = findViewById(R.id.layoutErrorPrompt);
		errorPromptView.setVisibility(View.VISIBLE);
		errorPromptView.setOnClickListener(view -> mPresenter.start());
	}

	/**
	 * 配置右下角的浮动按钮
	 * @param fabVO 控制浮动按钮子菜单项的实体对象
	 */
	@Override
	public void configFloatingActionButton(ParticularPresenter.FabVO fabVO) {
		if (mFloatingActionMenu == null) return;
		if (fabVO == null) {
			mFloatingActionMenu.setVisibility(View.GONE);
			return;
		}

		mFloatingActionMenu.setVisibility(View.VISIBLE);
		if (fabVO.hasReply) {
			mFloatingActionMenu.setReplyClickListener(View.VISIBLE, view ->
					executeScroll(getViewHeight(mHeadView) + getViewHeight(mContentView) + getViewHeight(mAttachmentView)));
		}
		else {
			mFloatingActionMenu.setReplyClickListener(View.GONE, null);
		}
		if (fabVO.hasAttachment) {
			mFloatingActionMenu.setAttachmentClickListener(View.VISIBLE, view ->
					executeScroll(getViewHeight(mHeadView) + getViewHeight(mContentView)));
		}
		else {
			mFloatingActionMenu.setAttachmentClickListener(View.GONE, null);
		}

		mFloatingActionMenu.setContentClickListener(View.VISIBLE, view -> executeScroll(getViewHeight(mHeadView)));

		if (fabVO.hasDuDu) {
			mFloatingActionMenu.setDuDuClickListener(View.VISIBLE, view -> {
				FRouter.build(ParticularActivity.this, "/x5/browser")
						.withString("extra", fabVO.duReplyUserIds)
						.withInt("moduleId", Func.Dudu)
						.go();
			});
		}
		else {
			mFloatingActionMenu.setDuDuClickListener(View.GONE, null);
		}

		mHandler.postDelayed(() -> {
			int contentHeight = mScrollView.getHeight();
			Point screenSize = new Point();
			mWindowManager.getDefaultDisplay().getSize(screenSize);
			int visibilityHeight = screenSize.y - mToolBar.getHeight();
			if (contentHeight < visibilityHeight) {
				if (!fabVO.hasDuDu) {
					mFloatingActionMenu.setVisibility(View.GONE);
					mFloatingActionMenu.setTag("NONE");
					return;
				}

				mFloatingActionMenu
						.setOnlyOneButton(R.drawable.menu_icon_dudu, R.color.detail_dudu_bg, R.color.detail_dudu_bg_press, view -> {
							FRouter.build(ParticularActivity.this, "/x5/browser")
									.withString("extra", fabVO.duReplyUserIds)
									.withInt("moduleId", Func.Dudu)
									.go();
						});
				return;
			}
			mFloatingActionMenu.setVisibility(View.VISIBLE);
		}, 200);
	}

	/**
	 * 设置详情页的头部信息
	 * @param headVO 头部信息实体对象
	 */
	@Override
	public void displayHeadInformation(ParticularPresenter.HeadVO headVO) {
		mHeadView.displayUserInfo(headVO);
	}

	/**
	 * 设置当前详情发送人的联系方式
	 * @param addressBookItem 联系人地址信息实体对象
	 */
	@Override
	public void configSendUserContactInfo(AddressBookItem addressBookItem) {
		mHeadView.configSendUserInformation(addressBookItem);
	}

	/**
	 * 根据 {@link ParticularPresenter} 根据请求的结果动态设置底部菜单。
	 * 仅仅设置文本，具体点击处理交由 {@link ParticularPresenter} 及其实现类处理。
	 * @param bottomMenuVO 底部菜单实体对象
	 */
	@Override
	public void configBottomMenu(ParticularPresenter.BottomMenuVO bottomMenuVO) {
		if (bottomMenuVO == null) {
			mBottomMenu.setVisibility(View.GONE);
			mBottomMenu.setTag("NONE");
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFloatingActionMenu.getLayoutParams();
			params.setMargins(0, 0, PixelUtil.dipToPx(6), 0);
			mFloatingActionMenu.setLayoutParams(params);
			return;
		}

		if (!TextUtils.isEmpty(bottomMenuVO.buttonText1)) {
			TextView menu1 = (TextView) findViewById(R.id.tvButton1);
			menu1.setVisibility(View.VISIBLE);
			menu1.setText(bottomMenuVO.buttonText1);
			menu1.setOnClickListener(mPresenter::handleBottomButton1);
		}

		if (!TextUtils.isEmpty(bottomMenuVO.buttonText2)) {
			TextView menu2 = (TextView) findViewById(R.id.tvButton2);
			menu2.setVisibility(View.VISIBLE);
			menu2.setText(bottomMenuVO.buttonText2);
			menu2.setOnClickListener(mPresenter::handleBottomButton2);
		}

		if (!TextUtils.isEmpty(bottomMenuVO.buttonText3)) {
			TextView menu3 = (TextView) findViewById(R.id.tvButton3);
			menu3.setVisibility(View.VISIBLE);
			menu3.setText(bottomMenuVO.buttonText3);
			menu3.setOnClickListener(mPresenter::handleBottomButton3);
		}

		if (!TextUtils.isEmpty(bottomMenuVO.buttonText4)) {
			TextView menu4 = (TextView) findViewById(R.id.tvButton4);
			menu4.setVisibility(View.VISIBLE);
			menu4.setText(bottomMenuVO.buttonText4);
			menu4.setOnClickListener(mPresenter::handleBottomButton4);
		}
	}

	/**
	 * 设置详情页的详细内容
	 * @param content 详情内容
	 * @param needSupplementStyle 是否需要添加 HTML 样式，部分内容会缺少表格线。
	 */
	@Override
	public void displayParticularContent(String content, boolean needSupplementStyle, String mobileFormUrl) {
		if (CoreZygote.getLoginUserServices() == null) return;
		String host = CoreZygote.getLoginUserServices().getServerAddress();
		if (TextUtils.isEmpty(mobileFormUrl)) {
			mScrollView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			mContentView.setParticularContent(host, content, needSupplementStyle);
		}
		else {
			mContentView.getWebView().setSwipeBackLayout(mSwipeBackLayout);
			mContentView.setParticularContentByUrl(host + content);
		}
	}

	/**
	 * 设置正文补充内容，该方法能被执行到，说明 supplyContents 肯定是有值的
	 * @param supplyContents 正文补充内容
	 */
	@Override
	public void displayContentSupplement(List<SupplyContent> supplyContents) {
		mContentView.setContentSupplement(supplyContents);
	}

	/**
	 * 设置正文修改内容，该方法能被执行到，同样说明 trailContents 肯定是有值得
	 * @param trailContents 正文修改内容
	 */
	@Override
	public void displayContentModify(List<TrailContent> trailContents) {
		mContentView.setContentModify(trailContents);
	}

	/**
	 * 设置详情附件信息，该方法能被执行到，说明当前详情绝壁是存在附件的。
	 * 这里使用 {@link ViewStub} 进行 IView 的延时加载，减少不必要的内存消耗。
	 * @param attachments 附件列表，该值不为空
	 */
	@Override
	public void displayAttachment(final List<NetworkAttachment> attachments) {
		if (attachments == null) {
			if (mAttachmentView != null && mAttachmentView.getVisibility() == View.VISIBLE) {
				mAttachmentView.setVisibility(View.GONE);
			}
			return;
		}

		if (mAttachmentView == null) {
			ViewStub attachmentViewStub = (ViewStub) findViewById(R.id.viewStubAttachment);
			mAttachmentView = attachmentViewStub.inflate();
		}

		TextView textView = (TextView) mAttachmentView.findViewById(R.id.tvParticularLabel);
		textView.setText(String.format(getResources().getString(R.string.attachment_count_tip), attachments.size()));
		NetworkAttachmentListFragment fragment = NetworkAttachmentListFragment.newInstance(true, attachments,
				attachment -> {
					if (AttachmentUtils.isAssociateMatters(attachment)) {
						showAssociateMatters((NetworkAttachment) attachment);
						return true;
					}
					return false;
				});

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.layoutAttachments, fragment)
				.show(fragment)
				.commitAllowingStateLoss();

		if (mAttachmentView.getVisibility() == View.GONE) {
			mAttachmentView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 设置原文评论列表数据，该方法能被执行到，说明当前存在评论列表。
	 * @param replies 评论列表
	 */
	@Override
	public void displayOriginalReplyList(List<Reply> replies) {
		if (mOriginalReplyView == null) {
			ViewStub replyViewStub = findViewById(R.id.viewStubOriginalReply);
			mOriginalReplyView = replyViewStub.inflate();
		}
		TextView textView = mOriginalReplyView.findViewById(R.id.tvParticularLabel);
		textView.setText(String.format(getString(R.string.originalreply_count_tip), replies.size()));

		RelativeElegantLayout relativeElegantLayout = mOriginalReplyView.findViewById(R.id.relativeElegantLayout);
		ReplyRelativeElegantAdapter replyAdapter = new ReplyRelativeElegantAdapter(this, R.layout.item_particular_reply, replies);
		replyAdapter.showReplyButton(false);
		setReplayClickEvent(relativeElegantLayout, replyAdapter);
	}

	/**
	 * 设置评论列表数据，该方法能被执行到，说明当前存在评论列表。
	 * @param replies 评论列表
	 * @param showReplyButton 通过该值判断是否能对评论进行回复。
	 */
	@Override
	public void displayReplyList(List<Reply> replies, boolean showReplyButton) {
		if (mReplyView == null) {
			ViewStub replyViewStub = (ViewStub) findViewById(R.id.viewStubReply);
			mReplyView = replyViewStub.inflate();
		}
		TextView textView = (TextView) mReplyView.findViewById(R.id.tvParticularLabel);
		textView.setText(String.format(getResources().getString(R.string.reply_count_tip), replies.size()));

		RelativeElegantLayout relativeElegantLayout = (RelativeElegantLayout) mReplyView.findViewById(R.id.relativeElegantLayout);
		ReplyRelativeElegantAdapter replyAdapter = new ReplyRelativeElegantAdapter(this, R.layout.item_particular_reply, replies);
		replyAdapter.showReplyButton(showReplyButton);
		setReplayClickEvent(relativeElegantLayout, replyAdapter);
	}

	private void setReplayClickEvent(RelativeElegantLayout relativeElegantLayout, ReplyRelativeElegantAdapter replyAdapter) {
		replyAdapter.setOnAttachmentItemClickListener((parentView, attachments) -> {
			NetworkAttachmentListDialog
					.newInstance(AttachmentBeanConverter.convert(attachments), attachment -> {
						if (AttachmentUtils.isAssociateMatters(attachment)) {
							showAssociateMatters((NetworkAttachment) attachment);
							return true;
						}
						return false;
					})
					.show(getSupportFragmentManager(), "FlyRise");
		});
		replyAdapter.setOnReplyButtonClickListener(mPresenter::clickToReply);
		relativeElegantLayout.setAdapter(replyAdapter);
	}

	/**
	 * 设置详情的相关阅读信息，该方法能被执行到，说明当前详情是存在相关阅读信息的。
	 * 相关阅读 和 评论列表，在布局中存在两个不同的 ViewStub 占位，
	 * 但是在 FloatingActionButton 和这里，使用的是同一个类成员变量 #mReplyView。
	 * @param relatedNews 相关阅读信息列表，该值不为空。
	 */
	@Override
	public void displayRelatedNews(final List<RelatedNews> relatedNews) {
		if (mReplyView == null) {
			ViewStub relatedNewsViewStub = (ViewStub) findViewById(R.id.viewStubRelatedNews);
			mReplyView = relatedNewsViewStub.inflate();
		}
		TextView textView = (TextView) mReplyView.findViewById(R.id.tvParticularLabel);
		RelativeElegantLayout relativeElegantLayout = (RelativeElegantLayout) mReplyView.findViewById(R.id.relativeElegantLayout);

		textView.setText(getResources().getString(R.string.lbl_text_related_news));
		relativeElegantLayout.setAdapter(new RelativeElegantAdapter<RelatedNews>(this, R.layout.item_related_news, relatedNews) {
			@Override
			public void initItemViews(View view, int position, RelatedNews relatedNew) {
				((TextView) view.findViewById(R.id.related_item_title)).setText(relatedNew.getTitle());
				((TextView) view.findViewById(R.id.related_item_time)).setText(relatedNew.getSendTime());
			}
		});
		relativeElegantLayout.setOnItemClickListener((position, view, object) -> {
			RelatedNews relatedNew = (RelatedNews) object;
			mPresenter.getParticularIntent().setTempBusinessId(relatedNew.getId());
			mPresenter.getParticularIntent().setTempMessageId(relatedNew.getMsgId());
			mPresenter.start();
		});
	}

	@Override
	public void isMettingReply() {
		isMettingReply = true;
	}

	/**
	 * 显示底部回复区域
	 */
	@Override
	public void displayReplyView(boolean withAttachment, String replyId, String btnText) {
		if (!TextUtils.equals(curReplyId, replyId)) {
			SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "");
		}
		curReplyId = replyId;
		if (mReplyEditView != null) {
			return;
		}

		mReplyEditView = new ParticularReplyEditView(this);
//        if (mReplyEditView == null) {
//            mReplyEditView = new ParticularReplyEditView(this);
//        }

		// 1. 初始化底部回复框
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		mWindowManager.addView(mReplyEditView, params);

		mReplyEditView.setFocusable(true);
		mReplyEditView.setMaxTextNum(isMettingReply ? 120 : -1);
		mReplyEditView.setWithAttachment(withAttachment);
		mReplyEditView.setOnTouchListener((view, event) -> {
			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				hindReplyVivewTime = System.currentTimeMillis();
				removeReplyEditView();
			}
			return true;
		});

		mReplyEditView.setOnKeyListener((v, keyCode, event) -> {
			FELog.i("OnKeyListener : " + event.getAction());
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
				removeReplyEditView();
			}
			return false;
		});

		if (!TextUtils.isEmpty(btnText)) {
			mReplyEditView.setSubmitButtonText(btnText);
		}

		// 弹出软键盘
		mHandler.postDelayed(showKeyBordRunnable, 50);

		// 2. 设置附件图标点击事件
		mReplyEditView.setOnAttachmentButtonClickListener(view -> {
			LuBan7.pufferGrenades(ParticularActivity.this, mLocalAttachments, null, CODE_OPEN_ATTACHMENT_PICKER);
		});

		// 3. 设置语音输入图标点击事件
		mReplyEditView.setOnRecordButtonClickListener(view -> {
			FePermissions.with(ParticularActivity.this)
					.permissions(new String[]{android.Manifest.permission.RECORD_AUDIO})
					.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
					.requestCode(PermissionCode.RECORD)
					.request();

		});

		// 4. 提交回复
		mReplyEditView.setOnReplySubmitClickListener(view -> {
			String replyContent = mReplyEditView.getReplyContent();
			if (TextUtils.isEmpty(replyContent)) {
				if (mLoadingDialog != null) {
					mLoadingDialog.hide();
				}
				dismissLoading(getResources().getString(R.string.reply_empty_msg));
				return;
			}

			replyContent = replyContent + getResources().getString(R.string.fe_from_android_mobile);

			final UploadManager uploadManager;
			showLoading();
			if (TextUtils.isEmpty(replyId)) {
				uploadManager = mPresenter.executeBusinessReply(mLocalAttachments, replyContent);
			}
			else {
				uploadManager = mPresenter.executeCommentReply(mLocalAttachments, replyContent, replyId);
			}

			if (mLoadingDialog != null) {
				mLoadingDialog.setOnDismissListener(() -> {
					if (uploadManager != null) {
						uploadManager.cancelTask();
						if (CommonUtil.nonEmptyList(mLocalAttachments)) {
							mLocalAttachments.clear();
							mReplyEditView.setAttachmentSize(0);
						}
						mLoadingDialog.removeDismissListener();
					}
				});
			}

			if (mReplyEditView != null) {
				removeReplyEditView();
			}
		});
	}

	private void removeReplyEditView() {
		if (mReplyEditView == null || mWindowManager == null) {
			return;
		}
		mHandler.removeCallbacks(showKeyBordRunnable);
		DevicesUtil.hideKeyboard(mReplyEditView.getReplyEditText());
		if (mReplyEditView.getWindowToken() != null) {
			mWindowManager.removeView(mReplyEditView);
		}
		mReplyEditView = null;
		isMettingReply = false;
	}

	@Override
	public void replySuccess() {
		dismissLoading(null);
		FEToast.showMessage(getResources().getString(R.string.reply_succ));
		if (mReplyEditView != null) {
			mReplyEditView.getReplyEditText().setText("");
			DevicesUtil.hideKeyboard(mReplyEditView.getReplyEditText());
			if (mReplyEditView.getWindowToken() != null) {
				mWindowManager.removeView(mReplyEditView);
			}
			isMettingReply = false;
		}

		if (CommonUtil.nonEmptyList(mLocalAttachments)) {
			mLocalAttachments.clear();
			if (mReplyEditView != null) {
				mReplyEditView.setAttachmentSize(0);
			}
		}
		SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "");
	}

	private Runnable showKeyBordRunnable = () -> ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
			.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

	@Override
	public void startIntent(Intent intent) {
		startActivityForResult(intent, CODE_START_INTENT);
	}

	@Override
	public void finishViewWithResult(Intent result) {
		setResult(0, result);
		finish();
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void showMeetingAttendUserInfo(View v, ParticularPresenter.MeetingAttendUserVO attendUserVO) {
		if (mMeetingAttendView == null) {
			mMeetingAttendView = new MeetingAttendView(this);
			mMeetingAttendView.setMeetingAttendUsers(attendUserVO);
		}
		if (mBubbleWindow == null) {
			mBubbleWindow = new BubbleWindow(mMeetingAttendView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		}
		mBubbleWindow.show(v);
	}

	@SuppressLint("RestrictedApi")
	@Override
	public void showCollaborationMenu(View view, List<Integer> permissions) {  // 右上角点击事件
		PopupMenu popupMenu = new PopupMenu(ParticularActivity.this, view);
		popupMenu.setGravity(Gravity.END);
		popupMenu.inflate(R.menu.popup_menu_collaboration);
		Menu menu = popupMenu.getMenu();
		if (!permissions.contains(R.id.action_revocation)) menu.removeItem(R.id.action_revocation);
		if (!permissions.contains(R.id.action_transmit)) menu.removeItem(R.id.action_transmit);
		if (!permissions.contains(R.id.action_circulate)) menu.removeItem(R.id.action_circulate);
		if (!permissions.contains(R.id.action_supplement)) menu.removeItem(R.id.action_supplement);
		if (!permissions.contains(R.id.action_view_flow)) menu.removeItem(R.id.action_view_flow);
		if (!permissions.contains(R.id.action_collection)) menu.removeItem(R.id.action_collection);
		if (!permissions.contains(R.id.action_collection_cancel)) menu.removeItem(R.id.action_collection_cancel);
		popupMenu.setOnMenuItemClickListener(menuItem -> {
			mPresenter.handlePopMenu(menuItem.getItemId(), ParticularActivity.this);
			return true;
		});
		MenuPopupHelper popupHelper = new MenuPopupHelper(this, (MenuBuilder) popupMenu.getMenu(), view);
		popupHelper.setForceShowIcon(true);
		popupHelper.setGravity(Gravity.END);
		popupHelper.show();
	}

	@SuppressLint("RestrictedApi")
	@Override public void showWorkPlanMenu(View view) {
		PopupMenu popupMenu = new PopupMenu(ParticularActivity.this, view);
		popupMenu.setGravity(Gravity.END);
		popupMenu.inflate(R.menu.popup_menu_workplan);
		if (!FunctionManager.hasPatch(Patches.PATCH_PLAN)) {
			popupMenu.getMenu().findItem(R.id.action_2collaboration).setVisible(false);
			popupMenu.getMenu().findItem(R.id.action_2schedule).setVisible(false);
		}
		popupMenu.setOnMenuItemClickListener(menuItem -> {
			mPresenter.handlePopMenu(menuItem.getItemId(), ParticularActivity.this);
			return true;
		});
		MenuPopupHelper popupHelper = new MenuPopupHelper(this, (MenuBuilder) popupMenu.getMenu(), view);
		popupHelper.setForceShowIcon(true);
		popupHelper.setGravity(Gravity.END);
		popupHelper.show();
	}

	@Override
	public void showConfirmDialog(String message, FEMaterialDialog.OnClickListener onClickListener) {
		new FEMaterialDialog.Builder(this).setMessage(message)
				.setPositiveButton(null, onClickListener)
				.setNegativeButton(null, null)
				.build()
				.show();
	}

	private void showAssociateMatters(NetworkAttachment attachment) {
		if (TextUtils.isEmpty(attachment.path)) {
			String type = attachment.type;

			ParticularIntent.Builder builder = new ParticularIntent.Builder(this)
					.setTargetClass(ParticularActivity.class)
					.setFromAssociate(true)
					.setParticularType(ParticularPresenter.PARTICULAR_MEETING)
					.setBusinessId(attachment.getId());

			if (Integer.parseInt(type) == 7) { // 关联事项，协同表单等
				builder.setListRequestType(RequestType.Done);
				builder.setParticularType(ParticularPresenter.PARTICULAR_COLLABORATION);
			}
			builder.create().start();
			return;
		}
		Intent intent = new Intent(this, AssociateCollaborationActivity.class);
		intent.putExtra(AssociateCollaborationActivity.ACTION_ASSOCIATE_URL, attachment.path);
		startActivity(intent);
	}

	@PermissionGranted(PermissionCode.RECORD)
	public void onRecordPermissionGranted() {
		if (mVoiceInput != null) mVoiceInput.show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CODE_OPEN_ATTACHMENT_PICKER && data != null) {
			mLocalAttachments = data.getStringArrayListExtra("extra_local_file");
			if (mReplyEditView != null) {
				mReplyEditView.setAttachmentSize(CommonUtil.isEmptyList(mLocalAttachments) ? 0 : mLocalAttachments.size());
			}
		}
		else if (requestCode == CODE_OPEN_ADDBODY && resultCode == RESULT_OK) {
			mPresenter.start();
		}
		else if (requestCode == CODE_SELECT_COLLECTION_FOLDER && resultCode == RESULT_OK && data != null) {
			String favoriteId = data.getStringExtra("favoriteId");
			String favoriteName = data.getStringExtra("favoriteName");
			mPresenter.addToFavoriteFolder(favoriteId, favoriteName);
		}
	}

	private int getViewHeight(View view) {
		if (view == null || view.getVisibility() != View.VISIBLE) {
			return 0;
		}

		return view.getMeasuredHeight();
	}

	private void executeScroll(int scrollY) {
		mScrollView.smoothScrollTo(0, scrollY);
	}

}
