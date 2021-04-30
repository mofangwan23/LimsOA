package cn.flyrise.feep.main;

import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_DOWNLOAD_ACTION;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.manager.XunFeiManager;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.event.EventCircleMessageRead;
import cn.flyrise.feep.event.EventJPushRefreshMessage;
import cn.flyrise.feep.event.EventJPushRefreshNoticeMessageMenu;
import cn.flyrise.feep.main.adapter.MainConversationAdapter;
import cn.flyrise.feep.main.message.MessageConstant;
import cn.flyrise.feep.main.message.MessageVO;
import cn.flyrise.feep.main.message.other.SystemMessageActivity;
import cn.flyrise.feep.main.message.task.TaskMessageActivity;
import cn.flyrise.feep.main.message.toberead.ToBeReadMessageActivity;
import cn.flyrise.feep.qrcode.view.QRCodeFragment;
import cn.flyrise.feep.retrieval.DataRetrievalActivity;
import cn.flyrise.feep.retrieval.DataRetrievalRepository;
import cn.flyrise.feep.retrieval.FeepRetrievalService;
import cn.flyrise.feep.utils.Patches;
import cn.squirtlez.frouter.FRouter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemLongClickListener;
import com.dk.view.badge.BadgeUtil;
import com.drop.WaterDropSwipRefreshLayout;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chatui.domain.OnEventConversationLoad;
import com.hyphenate.chatui.model.EmNotifierBean;
import com.hyphenate.chatui.ui.ChatActivity;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.busevent.EMChatEvent.BaseGroupEvent;
import com.hyphenate.easeui.busevent.EMChatEvent.GroupDestroyed;
import com.hyphenate.easeui.busevent.EMChatEvent.UserRemove;
import com.hyphenate.easeui.busevent.EMMessageEvent;
import com.hyphenate.easeui.busevent.EMMessageEvent.ImMessageRefresh;
import com.hyphenate.easeui.model.EaseAtMessageHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-04-01 10:00
 */
public class NewMainMessageFragment extends QRCodeFragment implements NewMainMessageContract.IView {

	private int mTotalUnReadCount;
	private int mCircleUnReadCount;
	private String[] mConversationMenu;

	//    private View mLoadingView;
	private RecyclerView mMessageRecycleView;
	private MainConversationAdapter mAdapter;
	private NewMainMessageHeadView mMessageHeadView;
	private NewMainMessageContract.IPresenter mPresenter;
	private AddressBookActionReceiver mActionReceiver;


	private static final int REFRESH_IM = 102;
	private static final int REFRESH_DELAY = 200;
	private static final int REFRESH_ALL = 103;
	private static final int REFRESH_ALL_DELAY = 800;

	private FEToolbar mToolBar;

	private WaterDropSwipRefreshLayout mSwipeRefreshLayout;
	private List<String> unReadCircleMessageIds;
	private boolean isStartLogin = true;
	private List<EMConversation> conversationLists;

	@SuppressLint("HandlerLeak")
	private Handler mRefreshHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case REFRESH_IM:
					mPresenter.fetchConversationList();
					break;
				case REFRESH_ALL:
					isStartLogin = false;
					mPresenter.start();
					break;
			}
		}
	};

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_new_main_message, container, false);
		bindView(contentView);
		bindData();
		mActionReceiver = new AddressBookActionReceiver();
		getActivity().registerReceiver(mActionReceiver, new IntentFilter(ADDRESS_BOOK_DOWNLOAD_ACTION));
		return contentView;
	}


	private void bindView(View contentView) {
		mToolBar = contentView.findViewById(R.id.toolBar);
		String messageTitle = getActivity().getResources().getString(R.string.top_message);
		if (mToolBar != null) {
			if (!TextUtils.isEmpty(messageTitle)) {
				mToolBar.setTitle(messageTitle);
				mToolBar.setNavigationVisibility(View.GONE);
			}

			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !DevicesUtil.isSpecialDevice()) {
				int statusBarHeight = DevicesUtil.getStatusBarHeight(getActivity());
				mToolBar.setPadding(0, statusBarHeight, 0, 0);
			}

//			if (FunctionManager.hasPatch(Patches.PATCH_NEW_APPLICATION)) {//7.0以上
//				mToolBar.setNavigationVisibility(View.GONE);
//				mToolBar.setRightIcon(R.mipmap.core_icon_zxing);
//
//			}
//			else {
//				mToolBar.setNavigationIcon(R.mipmap.core_icon_zxing);
//				mToolBar.setNavigationOnClickListener(v -> startScanActivity());
//				mToolBar.setRightIcon(R.mipmap.core_icon_microphone);
//			}

			mToolBar.setRightText(R.string.approval_title_string);
			mToolBar.setNavigationIcon(R.mipmap.core_icon_zxing);
			mToolBar.setNavigationOnClickListener(v -> startScanActivity());
			mToolBar.setRightTextClickListener(v -> {
				FRouter.build(getActivity(), "/collaboration/list").go();
			});
		}

		ViewGroup searchLayout = contentView.findViewById(R.id.layoutMessageSearch);
		ViewGroup searchLayoutMicrophone = contentView.findViewById(R.id.layoutMessageSearch_rl_search);
		TextView searchTextView = contentView.findViewById(R.id.layoutMessageSearch_tv_search);
		ImageView microphone = contentView.findViewById(R.id.layoutMessageSearch_iv_microphone);
		if (FunctionManager.hasPatch(Patches.PATCH_DATA_RETRIEVAL)) { //有信息检索
			if (FunctionManager.hasPatch(Patches.PATCH_NEW_APPLICATION)) { //7.0以上
				searchLayout.setVisibility(View.GONE);
				searchLayoutMicrophone.setVisibility(View.VISIBLE);
				searchTextView.setOnClickListener(view -> {
					Intent intent = new Intent(getActivity(), DataRetrievalActivity.class);
					getActivity().startActivity(intent);
				});
				microphone.setOnClickListener(v -> XunFeiManager.startRobot(getActivity()));
			}
			else {
				searchLayout.setVisibility(View.VISIBLE);
				searchLayoutMicrophone.setVisibility(View.GONE);
				searchLayout.setOnClickListener(view -> {
					Intent intent = new Intent(getActivity(), DataRetrievalActivity.class);
					getActivity().startActivity(intent);
				});
			}
		}
		else {
			searchLayout.setVisibility(View.GONE);
			searchLayoutMicrophone.setVisibility(View.GONE);
		}

		mMessageHeadView = new NewMainMessageHeadView(getActivity());
		mMessageRecycleView = contentView.findViewById(R.id.listView);
		mMessageRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
		mSwipeRefreshLayout = contentView.findViewById(R.id.swipeRefreshLayout);
	}

	private void bindData() {
		mSwipeRefreshLayout.setColorSchemeResources(R.color.defaultColorAccent);
		DataRetrievalRepository.init(FeepRetrievalService.class);
		mMessageHeadView.setOnTaskMessageClickListener(messageVO -> {                   // 任务消息
			boolean hasPatch = FunctionManager.hasPatch(Patches.PATCH_APPLICATION_BUBBLE);
			if (hasPatch) {
				TaskMessageActivity.start(getActivity());
			}
			else {
				SystemMessageActivity.start(getActivity(), MessageConstant.MISSION);
			}
		});

		mMessageHeadView.setOnUnReadMessageClickListener(messageVO -> {                  // 待阅消息
			boolean hasPatch = FunctionManager.hasPatch(Patches.PATCH_UNREAD_MESSAGE);
			if (hasPatch) {
				ToBeReadMessageActivity.start(getActivity());
			}
			else {
				SystemMessageActivity.start(getActivity(), MessageConstant.NOTIFY);
			}
		});
		// 系统消息
		mMessageHeadView.setOnSystemMessageClickListener(messageVO ->
				SystemMessageActivity.start(getActivity(), MessageConstant.SYSTEM));
		// 圈子消息
		mMessageHeadView.setOnGroupMessageClickListener(messageVO -> {
			// 兼容 651 之前的版本
			if (messageVO == null || TextUtils.isEmpty(messageVO.getUrl())) {
				((FEMainActivity) getActivity()).switchToAssociate();
			}
			else {
				SystemMessageActivity.start(getActivity(), MessageConstant.CIRCLE);
			}
		});
		// 聊天消息
		if (conversationLists == null) {
			conversationLists = new ArrayList<>();
		}
		mAdapter = new MainConversationAdapter(R.layout.view_new_main_message_head_item, conversationLists);
		mAdapter.addHeaderView(mMessageHeadView);
		mAdapter.setHasStableIds(true);
		mMessageRecycleView.setAdapter(mAdapter);
		mAdapter.setOnItemClickListener((adapter, view, position) -> {
			EMConversation conversation = mAdapter.getItem(position);
			String conversationId = conversation.conversationId();
			if (conversationId.equals(EMClient.getInstance().getCurrentUser())) {
				FEToast.showMessage(getResources().getString(R.string.Cant_chat_with_yourself));
				return;
			}
			int nums = conversation.getUnreadMsgCount();
			if (nums > 0) {
				FEApplication feApplication = (FEApplication) getActivity().getApplicationContext();
				int num = feApplication.getCornerNum() - nums;
				//角标
				BadgeUtil.setBadgeCount(getActivity(), num);
				feApplication.setCornerNum(num);
			}

			Intent intent = new Intent(getActivity(), ChatActivity.class);
			intent.putExtra(EaseUiK.EmChatContent.emChatID, conversationId);
			if (conversation.isGroup()) {
				boolean hasAtMeMsg = EaseAtMessageHelper.get().hasAtMeMsg(conversationId);
				if (hasAtMeMsg) {
					EaseAtMessageHelper.get().removeAtMeGroup(conversationId);
					mAdapter.notifyDataSetChanged();
				}
				intent.putExtra(EaseUiK.EmChatContent.emChatType, EaseUiK.EmChatContent.em_chatType_group);
			}
			startActivity(intent);
		});

		mAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
				EMConversation conversation = (EMConversation) mAdapter.getItem(position);
				if (mConversationMenu == null) {
					mConversationMenu = new String[]{getResources().getString(R.string.delete)};
				}
				new FEMaterialDialog.Builder(getActivity())
						.setWithoutTitle(true)
						.setItems(mConversationMenu, (dialog, v, index) -> {
							dialog.dismiss();
							deleteConversation(conversation);
						})
						.setCancelable(true)
						.build()
						.show();
				return true;
			}
		});

		mSwipeRefreshLayout.setOnRefreshListener(() -> {
			// 刷新数据
			mPresenter.fetchMessageList();
			mPresenter.fetchConversationList();
			Observable.timer(2, TimeUnit.SECONDS)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(time -> mSwipeRefreshLayout.setRefreshing(false));
		});

		mPresenter = new NewMainMessagePresenter(getActivity(), this);
		mAdapter.setOnDragCompeteListener(new MainConversationAdapter.OnDragCompeteListener() {
			@Override public void onDragCompete(EMConversation conversation) {
				if (conversation == null) {return;}
				conversation.markAllMessagesAsRead();
				fetchConversationList();
			}

			@Override public void onDownDrag(boolean isDownDrag) {
				mSwipeRefreshLayout.setMoveDrop(isDownDrag);
			}
		});
		//消息标记已读
		mMessageHeadView.setOnDragCompeteListener(new NewMainMessageHeadView.OnDragCompeteListener() {
			@Override public void onDragCompete(MessageVO messageVO) {
				mPresenter.allMessageListRead(messageVO.getCategory());
			}

			@Override public void onDownDrag(boolean isDownDrag) {
				mSwipeRefreshLayout.setMoveDrop(isDownDrag);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		SpUtil.put("notification_acitivity", true);
		mRefreshHandler.sendEmptyMessageDelayed(REFRESH_ALL, isStartLogin ? 0 : REFRESH_ALL_DELAY);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onReceiveJPushMessage(EventJPushRefreshMessage refreshMessage) {                        // 收到机关推送
		mPresenter.fetchMessageList();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onHuanXinLoginSuccess(OnEventConversationLoad code) {                                   // 登录成功狗日的环信
		fetchConversationList();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onImMessageListRefresh(ImMessageRefresh refresh) {                                   // 登录成功狗日的环信
		fetchConversationList();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onImMessageListRefresh(BaseGroupEvent refresh) {                                   // 登录成功狗日的环信
		fetchConversationList();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onImMessageListRefresh(UserRemove event) {                                   // 登录成功狗日的环信
		fetchConversationList();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onImMessageListRefresh(GroupDestroyed event) {                                   // 登录成功狗日的环信
		fetchConversationList();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCmdGroupNameChange(EMMessageEvent.CmdChangeGroupName event) {
		fetchConversationList();
	}

	/**
	 * 消除圈子消息气泡
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void cleanCircleMessageNotify(EventCircleMessageRead event) {
		if (event != null && "hasReadCircleMessage".equals(event.getValue())) {
			if (event.getMessageIds() != null) {
				mPresenter.circleMessageListRead(event.getMessageIds());
			}
			else {
				mPresenter.circleMessageListRead(unReadCircleMessageIds);
			}
		}
	}


	public void fetchConversationList() {
		if (mRefreshHandler.hasMessages(REFRESH_IM)) {
			return;
		}
		mRefreshHandler.sendEmptyMessageDelayed(REFRESH_IM, REFRESH_DELAY);
	}

	@Override
	public void onMessageLoadSuccess(List<MessageVO> messageVOs) {
		// 刷新数据
		mMessageHeadView.setDataSource(messageVOs);
		this.setUnReadMessageCount();
		isEaseNotifier();
	}

	//所有消息标记为已读
	public void allMessageRead() {
		mPresenter.allMessageListRead(ALL_MESSAGE_READ);
	}


	@Override
	public void onConversationLoadSuccess(List<EMConversation> conversationLists) {
		this.conversationLists.clear();
		this.conversationLists.addAll(conversationLists);
		this.setUnReadMessageCount();
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 由于没有很好的方法监听到，
	 * 环信和OA的数据是否全部加载成功，
	 * 所以只能放到环信列表加载的时候进行通知栏监听。
	 * 是否是通知栏进来的消息
	 */
	private void isEaseNotifier() {
		if (!IMHuanXinHelper.getInstance().isImLogin()) {
			return;
		}
		EmNotifierBean notifierBean = FEApplication.getEmNotifierBean();
		if (notifierBean == null) {
			return;
		}
		FEApplication.setEmNotifierBean(null);
		String chatId = notifierBean.emChatID;
		if (TextUtils.isEmpty(chatId)) {
			return;
		}
		if (chatId.equals(EMClient.getInstance().getCurrentUser())) {
			return;
		}
		int type = notifierBean.emChatType;
		if (type == EaseUiK.EmChatContent.em_chatType_single || type == EaseUiK.EmChatContent.em_chatType_group) {
			Intent intent = new Intent(getActivity(), ChatActivity.class);
			intent.putExtra(EaseUiK.EmChatContent.emChatID, chatId);
			intent.putExtra(EaseUiK.EmChatContent.emChatType, type);
			startActivity(intent);
		}
	}

	@Override
	public void showLoading() {
		LoadingHint.show(getActivity());
	}

	@Override
	public void hideLoading() {
		LoadingHint.hide();
	}

	@Override
	public void onCircleMessageIdListSuccess(List<String> messageIds) {
		if (messageIds != null) unReadCircleMessageIds = messageIds;
	}

	@Override
	public void onCircleMessageReadSuccess() {
		mTotalUnReadCount = mMessageHeadView.getUnReadMessageCount() + IMHuanXinHelper.getInstance().getUnreadCount();
		mMessageHeadView.setCircleCount(0);
		EventJPushRefreshNoticeMessageMenu nmm = new EventJPushRefreshNoticeMessageMenu("20", String.valueOf(mTotalUnReadCount),
				String.valueOf(0));
		EventBus.getDefault().post(nmm);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		SpUtil.put("notification_acitivity", false);
		EventBus.getDefault().unregister(this);
		mRefreshHandler.removeMessages(REFRESH_ALL);
		mRefreshHandler.removeMessages(REFRESH_IM);
		if (mActionReceiver != null) {
			getActivity().unregisterReceiver(mActionReceiver);
			mActionReceiver = null;
		}
	}

	private void setUnReadMessageCount() {
		mTotalUnReadCount = mMessageHeadView.getUnReadMessageCount() + IMHuanXinHelper.getInstance().getUnreadCount();
		mCircleUnReadCount = mMessageHeadView.getCircleUnReadMessageCount();
		EventJPushRefreshNoticeMessageMenu nmm = new EventJPushRefreshNoticeMessageMenu("20", String.valueOf(mTotalUnReadCount),
				String.valueOf(mCircleUnReadCount));
		EventBus.getDefault().post(nmm);
		FEApplication feApplication = (FEApplication) getActivity().getApplicationContext();
		feApplication.setCornerNum(mTotalUnReadCount);
		BadgeUtil.setBadgeCount(getContext(), mTotalUnReadCount);//角标
		if (mCircleUnReadCount != 0) {
			mPresenter.requestCircleMessageList(mCircleUnReadCount, 1);
		}

	}

	private void deleteConversation(EMConversation conversation) {
		if (conversation == null) {
			return;
		}
		if (conversation.getType() == EMConversation.EMConversationType.GroupChat) {
			EaseAtMessageHelper.get().removeAtMeGroup(conversation.conversationId());
		}
		try {
			EMClient.getInstance().chatManager().deleteConversation(conversation.conversationId(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mPresenter.fetchConversationList();
	}


	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
//			mPresenter.start();
		}
	}

	private class AddressBookActionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			fetchConversationList();
		}
	}

}
