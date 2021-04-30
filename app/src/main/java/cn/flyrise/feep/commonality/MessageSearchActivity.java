package cn.flyrise.feep.commonality;

import static cn.flyrise.feep.core.common.X.RequestType.Activity;
import static cn.flyrise.feep.core.common.X.RequestType.Announcement;
import static cn.flyrise.feep.core.common.X.RequestType.Done;
import static cn.flyrise.feep.core.common.X.RequestType.Knowledge;
import static cn.flyrise.feep.core.common.X.RequestType.Meeting;
import static cn.flyrise.feep.core.common.X.RequestType.News;
import static cn.flyrise.feep.core.common.X.RequestType.Schedule;
import static cn.flyrise.feep.core.common.X.RequestType.Sended;
import static cn.flyrise.feep.core.common.X.RequestType.ToDo;
import static cn.flyrise.feep.core.common.X.RequestType.ToDoDispatch;
import static cn.flyrise.feep.core.common.X.RequestType.ToDoNornal;
import static cn.flyrise.feep.core.common.X.RequestType.ToDoRead;
import static cn.flyrise.feep.core.common.X.RequestType.ToSend;
import static cn.flyrise.feep.core.common.X.RequestType.Trace;
import static cn.flyrise.feep.core.common.X.RequestType.Vote;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.adapter.FEMessageListAdapter;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.base.component.FESearchListActivity;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.flyrise.feep.event.EventMessageDisposeSuccess;
import cn.flyrise.feep.knowledge.util.KnowledgeUtil;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.particular.ParticularIntent;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;
import cn.flyrise.feep.utils.Patches;
import cn.squirtlez.frouter.FRouter;
import cn.squirtlez.frouter.annotations.Route;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 陈冕
 * 功能：所有消息搜索
 * Created by Administrator on 2016-3-17.
 */
@Route("/message/search")
public class MessageSearchActivity extends FESearchListActivity<FEListItem> {

	private FEMessageListAdapter listAdapter;
	private int requestType;
	private ViewGroup mWaterMarkContainer;

	public static final String REQUESTTYPE = "request_type";
	public static final String REQUESTNAME = "request_NAME";

	private MessageSearchPresenter presenter;

	public String messageId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		presenter.refreshListData();
	}

	@Override
	public void bindView() {
		mWaterMarkContainer = (ViewGroup) findViewById(R.id.layoutContentView);
		super.bindView();
	}

	@Override
	public void bindData() {
		Intent intent = getIntent();
		int type;
		String name;
		if (intent != null) {
			type = intent.getIntExtra(REQUESTTYPE, -1);
			name = intent.getStringExtra(REQUESTNAME);
			if (!TextUtils.isEmpty(name)) {
				et_Search.setHint("搜索" + name);
			}
			requestType = type;
			if (requestType == ToDo) {
				et_Search.setHint("搜索待办");
			}
			else if (requestType == Done) {
				et_Search.setHint("搜索已办");
			}
			else if (requestType == Sended) {
				et_Search.setHint("搜索已发");
			}
		}
		listAdapter = new FEMessageListAdapter();
		setAdapter(listAdapter);
		presenter = new MessageSearchPresenter(this, this, requestType);
		setPresenter(presenter);

		String keyword = intent.getStringExtra("keyword");
		if (!TextUtils.isEmpty(keyword)) {
			et_Search.setText(keyword);
			et_Search.setSelection(keyword.length());
			searchKey = keyword;
			myHandler.post(searchRunnable);
		}
		else {
			myHandler.postDelayed(() -> DevicesUtil.showKeyboard(et_Search), 500);
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		listAdapter.setOnItemClickListener((view, object) -> {
			final FEListItem item = (FEListItem) object;
			View focusView = getCurrentFocus();
			if (view != null) {
				DevicesUtil.hideKeyboard(focusView);
			}
			openDetailActivity(MessageSearchActivity.this, item);
		});

		et_Search.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

			@Override public void afterTextChanged(Editable s) {
				if (s.length() == 0 && isNeedWaterMark()) {
					WMStamp.getInstance().draw(mWaterMarkContainer, listView.getLoadMoreRecyclerView());
				}
			}
		});
		robotSearchMessage(getIntent());
	}

	private void robotSearchMessage(Intent intent) {
		if (intent == null || et_Search == null) {
			return;
		}
		String userName = intent.getStringExtra("user_name");
		if (TextUtils.isEmpty(userName)) {
			return;
		}
		et_Search.setText(userName);
		et_Search.setSelection(userName.length());
	}

	/**
	 * 打开相对应的详情界面
	 */
	private void openDetailActivity(Context context, FEListItem item) {
		final int requestType = this.requestType;
		messageId = item.getId();
		switch (requestType) {
			case ToDo:
			case Done:
			case Trace:
			case ToSend:
			case Sended:
			case ToDoDispatch:
			case ToDoNornal:
			case ToDoRead:
//                CollaborationDetailActivity.setCollaborationInfo(item.getId(), requestType, item.getMsgId());
//                final Intent collintent = new Intent(context, CollaborationDetailActivity.class);
//                context.startActivity(collintent);
				new ParticularIntent.Builder(MessageSearchActivity.this)
						.setParticularType(ParticularPresenter.PARTICULAR_COLLABORATION)
						.setTargetClass(ParticularActivity.class)
						.setBusinessId(item.getId())
						.setListRequestType(requestType)
						.setMessageId(item.getMsgId())
						.create()
						.start();

				break;
			case News:
			case Announcement:
				new ParticularIntent.Builder(MessageSearchActivity.this)
						.setTargetClass(ParticularActivity.class)
						.setParticularType(requestType == 5
								? ParticularPresenter.PARTICULAR_NEWS
								: ParticularPresenter.PARTICULAR_ANNOUNCEMENT)
						.setBusinessId(item.getId())
						.setFEListItem(item)
						.setListRequestType(requestType)
						.create()
						.start();
				break;
			case Meeting:
				new ParticularIntent.Builder(MessageSearchActivity.this)
						.setTargetClass(ParticularActivity.class)
						.setParticularType(ParticularPresenter.PARTICULAR_MEETING)
						.setBusinessId(item.getId())
						.setMessageId(item.getMsgId())
						.setRequestCode(0)
						.create()
						.start();

				break;
			case Knowledge:// 知识管理
				KnowledgeUtil.openReceiverFileActivity(item.getMsgId(), item.getId(), context);
				break;
			case Vote:// 投票管理
				FRouter.build(MessageSearchActivity.this, "/x5/browser")
						.withString("businessId", item.getId())
						.withString("messageId", item.getMsgId())
						.withInt("moduleId", Func.Vote)
						.go();
				break;
			case Activity:// 活动管理
				FRouter.build(MessageSearchActivity.this, "/x5/browser")
						.withString("businessId", item.getId())
						.withString("messageId", item.getMsgId())
						.withInt("moduleId", Func.Activity)
						.go();
				break;
			case Schedule:// 日程管理
				FRouter.build(MessageSearchActivity.this, "/x5/browser")
						.withString("businessId", item.getId())
						.withString("messageId", item.getMsgId())
						.withInt("moduleId", Func.Schedule)
						.go();
				break;
			default:
				FEToast.showMessage(getResources().getString(R.string.phone_does_not_support_message));
				break;
		}
	}

	@Override
	public void refreshListData(List<FEListItem> dataList) {
		super.refreshListData(dataList);
		if (isNeedWaterMark()) {
			WMStamp.getInstance().draw(mWaterMarkContainer, listView.getLoadMoreRecyclerView());
		}
	}

	@Override
	public void loadMoreListData(List<FEListItem> dataList) {
		super.loadMoreListData(dataList);
		if (isNeedWaterMark()) {
			WMStamp.getInstance().draw(mWaterMarkContainer, listView.getLoadMoreRecyclerView());
		}
	}

	private boolean isNeedWaterMark() {
		return requestType == 0 || requestType == 1 || requestType == 4 || requestType == 23 || requestType == 24 || requestType == 25;
	}

	@Override protected int statusBarColor() {
		return Color.BLACK;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void eventRefreshList(EventMessageDisposeSuccess disposeSuccess) {
		if (!FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST)) return;
		if (requestType != RequestType.ToDo || TextUtils.isEmpty(messageId)) return;
		listAdapter.removeMessage(messageId);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		EventMessageDisposeSuccess success = new EventMessageDisposeSuccess();
		success.isRefresh = true;
		EventBus.getDefault().post(success);
		WMStamp.getInstance().clearWaterMark(this);
	}
}
