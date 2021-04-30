/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-7
 * 流程选择界面
 */
package cn.flyrise.feep.collaboration.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.library.view.Panel;
import cn.flyrise.android.library.view.Panel.SlidingScrollListener;
import cn.flyrise.android.library.view.ResizeTextView;
import cn.flyrise.android.library.view.SearchBar;
import cn.flyrise.android.library.view.TransformLayout;
import cn.flyrise.android.library.view.addressbooklistview.AddressBookListView;
import cn.flyrise.android.library.view.addressbooklistview.AddressBookListView.OnLoadListener;
import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.library.view.pulltorefreshlistview.FEPullToRefreshListView;
import cn.flyrise.android.library.view.pulltorefreshlistview.FEPullToRefreshListView.OnItemLongClickListener;
import cn.flyrise.android.protocol.entity.CollaborationSendDoRequest;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.android.protocol.model.Flow;
import cn.flyrise.android.protocol.model.FlowNode;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.view.Avatar;
import cn.flyrise.feep.collaboration.view.PersonPositionSwitcher;
import cn.flyrise.feep.collaboration.view.workflow.WorkFlowNode;
import cn.flyrise.feep.collaboration.view.workflow.WorkFlowView;
import cn.flyrise.feep.commonality.PersonSearchActivity;
import cn.flyrise.feep.commonality.adapter.PersonAdapter;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.X.AddressBookFilterType;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.X.CollaborationType;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.flyrise.feep.form.util.FormDataProvider;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import java.util.ArrayList;
import java.util.UUID;

public class WorkFlowActivity extends BaseActivity {

	private AddressBookListView personListView;                      // 左侧可供添加人员列表
	private WorkFlowView wfv;                                               // 流程图
	private Panel mPanel;                                                    // 抽屉
	private RelativeLayout searchIcon;
	private SearchBar searchBar;
	private ResizeTextView back_Tv;
	private PersonPositionSwitcher switcher;

	private View helperLayout;
	private Editor editor;
	private final String GUIDE_STATE_KEY = "WORKFLOW_GUIDE_STATE";
	private String collaborationGUID;
	private FEToolbar mToolBar;

	public static final String SEND_BUTTON_KEY = "sendButtonKey";
	public static final String COLLABORATIONID_INTENT_KEY = "collaborationID";
	public static final String COLLABORATIONID_GUID = "collaborationGUID";
	private TransformLayout searchBarTransformer;
	private TransformLayout flowViewTransformer;
	public static boolean isPerson = true;

	private ViewGroup mWaterMarkContainer;

	private final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 10010) {
				back_Tv.setText(getResources().getString(R.string.flow_reload));
			}
			if (msg.what == 10011) {
				personListView.goBack();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.collaboration_workflow);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolBar = toolbar;
		this.mToolBar.setNavigationOnClickListener(v -> {
			wfv.getBubbleWindow().dismisWithoutAnima();
			finish();
		});
		mToolBar.setRightText(R.string.collaboration_recorder_ok);
		mToolBar.setRightTextClickListener(v -> {
			resultData = wfv.getResult();
			modify = wfv.hasModify();
			wfv.getBubbleWindow().dismisWithoutAnima();
			setResult(RESULT_OK);
			finish();
		});
	}

	@Override
	public void bindView() {
		mWaterMarkContainer = (ViewGroup) findViewById(R.id.layoutContentView);
		wfv = (WorkFlowView) findViewById(R.id.workFlowView1);
		mPanel = (Panel) findViewById(R.id.panel);
		searchBar = (SearchBar) findViewById(R.id.searchBar);
		back_Tv = (ResizeTextView) findViewById(R.id.back);
		switcher = (PersonPositionSwitcher) findViewById(R.id.switcher);
		helperLayout = findViewById(R.id.collaboration_workflow_helper);
		helperBnt = (Button) findViewById(R.id.collaboration_workflow_helper_button);
		personListView = (AddressBookListView) findViewById(R.id.list);
		personListView.setDiverHide();
		String waterMark = WMStamp.getInstance().getWaterMarkText();
		if (!TextUtils.isEmpty(waterMark)) {
			personListView.setOnListDataChangeListener(() -> {
				WMStamp.getInstance().draw(mWaterMarkContainer, personListView.getRefreshableView());
			});
		}

		searchBarTransformer = (TransformLayout) findViewById(R.id.searchBarTransformer);
		flowViewTransformer = (TransformLayout) findViewById(R.id.flowViewTransformer);
		searchIcon = (RelativeLayout) findViewById(R.id.search_icon);
	}

	@Override
	public void bindData() {
		wfv.setScrollableOutsideChile(true);
		setGuide(); // 显示或隐藏帮助
		getIntentData();
		back_Tv.setMaxLines(2);
		if (initData == null) {
		    /*--用户信息--*/
			final String UserID = CoreZygote.getLoginUserServices().getUserId();
			String UserName = CoreZygote.getLoginUserServices().getUserName();
			if (UserName == null) {
				UserName = "null";
			}
			/*--End--*/
			initData = new Flow();
			final FlowNode ni = new FlowNode();
			ni.setName(UserName);
			ni.setType(AddressBookType.Staff);
			ni.setValue(UserID);
			ni.setGUID(UUID.randomUUID().toString());
			initData.setNodes(new ArrayList<>());
			initData.getNodes().add(ni);
		}
		wfv.setInitData(initData, currentFlowNodeGUID);
		back_Tv.setText(R.string.flow_loading);
		mToolBar.setTitle(getResources().getString(R.string.flow_titleadd));
		int dimension = (int) getResources().getDimension(R.dimen.mdp_110);

		if (function == COLLABORATION_SHOW) {
			wfv.setSpaceLeft((int) getResources().getDimension(R.dimen.mdp_10));
			mToolBar.setTitle(getResources().getString(R.string.flow_titleshow));
			mPanel.setVisibility(View.GONE);
			wfv.setSelected(false);
			searchIcon.setVisibility(View.GONE);
			searchBar.setVisibility(View.GONE);
		}
		else {
			mPanel.setOpen(true, false);
			final PersonAdapter personAdapter = new PersonAdapter(this, myHandler, false, Avatar.NAMERIGHT);
			personListView.setAdapter(personAdapter);
			if (function == COLLABORATION_NEW) {
				wfv.setSpaceLeft(dimension);
				wfv.setLockExist(false);
				wfv.setSelected(true);
				mToolBar.setTitle(R.string.flow_titlenew);
			}
			else if (function == COLLABORATION_ADDSIGN) {
				wfv.setSpaceLeft(dimension);
				wfv.setLockExist(true);
				mToolBar.setTitle(R.string.flow_titleaddsign);
			}
			else if (function == COLLABORATION_TRANSMIT) {
				wfv.setSpaceLeft(dimension);
				wfv.setLockExist(false);
				wfv.setSelected(true);
				mToolBar.setTitle(R.string.flow_titletranspond);
			}

			personListView.setPostToCurrentDepartment(true);
			personListView.setFilterType(AddressBookFilterType.Authority);
			personListView.setPersonType(AddressBookType.Staff);
			personListView.setAutoJudgePullRefreshAble(true);
			personListView.requestRoot(1);
			LoadingHint.show(WorkFlowActivity.this);
		}
	}

	/**
	 * 发送按钮的处理方法
	 */
	private void getIntentData() {
		final Intent intent = getIntent();
		isHasSendButton = intent.getBooleanExtra(SEND_BUTTON_KEY, false);
		collaborationID = intent.getStringExtra(COLLABORATIONID_INTENT_KEY);
		collaborationGUID = intent.getStringExtra(COLLABORATIONID_GUID);
	}

	/**
	 * --用户提示--
	 */
	private void setGuide() {
		final SharedPreferences preferences = getSharedPreferences("guide_state", 0);
		editor = preferences.edit();
		if (preferences.getInt(GUIDE_STATE_KEY, 0) == 0 && COLLABORATION_SHOW != function) {
			helperLayout.setVisibility(View.VISIBLE);
		}
		else {
			helperLayout.setVisibility(View.GONE);
		}
	}

	private static final Handler handler = new Handler();

	@Override
	public void bindListener() {
		if (isHasSendButton) {
			mToolBar.setRightText(R.string.submit);
			mToolBar.setRightTextClickListener(v -> {
				if (wfv != null && wfv.hasModify()) {
					additionalRequest();
				}
				else {
					FEToast.showMessage(getString(R.string.collaboration_add_message));
				}
			});

		}
		back_Tv.setOnClickListener(v -> {
			if (personListView.isCanGoBack()) {
				LoadingHint.show(WorkFlowActivity.this);
				personListView.goBack();
			}
		});
		switcher.setOnBoxClickListener(b -> {
			personListView
					.setPersonType(b ? AddressBookType.Staff : AddressBookType.Position);
			isPerson = b;
		});
		searchIcon.setOnClickListener(v -> {
			PersonSearchActivity.setPersonSearchActivity(getSearchType(), wfv);
			Intent intent = new Intent(WorkFlowActivity.this, PersonSearchActivity.class);
			intent.putExtra(PersonSearchActivity.REQUESTNAME, getResources().getString(R.string.flow_titleadd));
			startActivity(intent);
		});
		helperBnt.setOnClickListener(v -> {
			final AlphaAnimation exitAnim = new AlphaAnimation(1, 0);
			exitAnim.setDuration(150);
			helperLayout.setAnimation(exitAnim);
			editor.putInt(GUIDE_STATE_KEY, 1);
			editor.commit();
			handler.postDelayed(() -> {
				helperLayout.setVisibility(View.GONE);
			}, 150);
		});

		helperLayout.setOnTouchListener((v, event) -> {
			return true;/* 防止在用户提示显示的情况下还可以点击用户提示底下的控件 */
		});
		personListView.setOnTouchListener(touchTransferListener);
		personListView.setOnItemLongClickListener(touchAddNode);
		personListView.setOnLoadListener(new OnLoadListener() {
			@Override
			public void Loading(AddressBookListItem bookListItem) {
				if (!LoadingHint.isLoading()) {
					LoadingHint.show(WorkFlowActivity.this);
				}
				back_Tv.setText(getResources().getString(R.string.flow_loading));
			}

			@Override
			public void Loaded(AddressBookListItem bookListItem) {
				if (LoadingHint.isLoading()) {
					LoadingHint.hide();
				}
				if (bookListItem == null) {
					myHandler.sendEmptyMessage(10010);
					return;
				}
				if (bookListItem.getListDatas() == null) {
					myHandler.sendEmptyMessage(10010);
				}
				final String name = bookListItem.getItemName();
				if (name == null) {
					back_Tv.setText(getResources().getString(R.string.flow_btnback));
				}
				else if ("-1".equals(name)) {
					back_Tv.setText(getResources().getString(R.string.flow_root));
				}
				else if (bookListItem.getTotalNums() != 0) {
					back_Tv.setText(name);
				}
			}
		});
		mPanel.setSlidingScrollListener(new SlidingScrollListener() {
			private float lastTrack = 0.017f; // 防止重绘界面相互调用导致重绘死循环

			@Override
			public void onScroll(int contentWidth, int contentHight, float trackX, float trackY) {
				if (lastTrack != trackX) {
					searchBarTransformer.setBorder((int) (contentWidth + trackX), 0, 0, 0);
					flowViewTransformer.setBorder((int) trackX, 0, 0, 0);
					lastTrack = trackX;
				}
			}
		});
	}

	private int getSearchType() {
		return (isPerson ? AddressBookType.Staff : AddressBookType.Position);
	}

	private final OnTouchListener touchTransferListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					wfv.setNotScroll(true);
					break;
				case MotionEvent.ACTION_UP:
					wfv.setNotScroll(false);
					if (personListView != null) {
						personListView.setRefreshAble();
					}
					break;
			}
			wfv.dispatchTouchEvent(event);
			return false;
		}
	};
	private final OnItemLongClickListener touchAddNode = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(FEPullToRefreshListView parent, View view, int position, long id) {
			if (personListView != null) {
				personListView.setMode(PullToRefreshBase.Mode.DISABLED);
			}
			final AddressBookItem addingNode = ((PersonAdapter) parent.getAdapter()).getItem(position).getAddressBookItem();
			final WorkFlowNode wfn = new WorkFlowNode();
			FELog.i(addingNode.toString());
			wfn.setNodeName(addingNode.getName());
			wfn.setNodeId(addingNode.getId());
			wfn.setType(addingNode.getType());
			wfn.setImageHref(addingNode.getImageHref());
			wfv.addNewNode(wfn);
			return true;
		}
	};

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 当在抽屉处于隐藏并且搜索结果显示的状态下，出现抽屉的触摸事件，先让搜索结果隐藏
				if (searchBar.isResuleShowing() && !mPanel.isOpen()) {
					cancelPanelTouch(event);
					return true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				wfv.setTouchable();
				break;
			default:
				break;
		}
		return super.dispatchTouchEvent(event);
	}

	/**
	 * 取消抽屉的触摸事件
	 */
	private void cancelPanelTouch(MotionEvent event) {
		final MotionEvent me = MotionEvent.obtain(event);
		me.setAction(MotionEvent.ACTION_CANCEL);
		mPanel.dispatchTouchEvent(me);
	}

	/**
	 * “已发”列表加签请求
	 */
	private void additionalRequest() {
		final FileRequest fileRequest = new FileRequest();
		final CollaborationSendDoRequest requestcontent = new CollaborationSendDoRequest();
		requestcontent.setId(collaborationID);
		requestcontent.setAttachmentGUID(collaborationGUID);
		requestcontent.setFlow(wfv.getResult());
		requestcontent.setRequestType(CollaborationType.SendedAdditional);
		fileRequest.setRequestContent(requestcontent);

		final ArrayList<Integer> typeList = new ArrayList<>();
		typeList.add(RequestType.Sended);

		new UploadManager(this)
				.fileRequest(fileRequest)
				.progressUpdateListener(new OnProgressUpdateListenerImpl() {
					@Override
					public void onPreExecute() {
						LoadingHint.show(WorkFlowActivity.this);
					}

					@Override
					public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
						int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
						LoadingHint.showProgress(progress);
					}
				})
				.responseCallback(new ResponseCallback<ResponseContent>() {
					@Override
					public void onCompleted(ResponseContent responseContent) {
						LoadingHint.hide();
						final String errorCode = responseContent.getErrorCode();
						final String errorMessage = responseContent.getErrorMessage();
						if (!TextUtils.equals("0", errorCode)) {
							FEToast.showMessage(errorMessage);
							return;
						}

						FEToast.showMessage(getResources().getString(R.string.message_operation_alert));
						startActivity(FormDataProvider.buildIntent(WorkFlowActivity.this, FEMainActivity.class));
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						LoadingHint.hide();
					}
				})
				.execute();

	}

	/*--点击键盘返回键，搜索键等--*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				wfv.getBubbleWindow().dismisWithoutAnima();
				break;
			case KeyEvent.KEYCODE_SEARCH:
				// 此处添加搜索功能
				break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPanel != null) {
			mPanel.destroy();
			mPanel = null;
		}
		WMStamp.getInstance().clearWaterMark(mWaterMarkContainer);

		initData = null;
	}

	// ---------------以下是公开的可用方法--------------- //
	private static Flow initData;
	private static String currentFlowNodeGUID;

	/**
	 * 显示已有流程(Flow)
	 */
	public static void setInitData(Flow initData, String currentFlowNodeGUID) {
		if (initData != null) {
			WorkFlowActivity.initData = initData.clone();
		}
		WorkFlowActivity.currentFlowNodeGUID = currentFlowNodeGUID;
	}

	/**
	 * 新建流程
	 */
	public static final int COLLABORATION_NEW = 1;

	/**
	 * 显示流程
	 */
	public static final int COLLABORATION_SHOW = 2;

	/**
	 * 协同加签
	 */
	public static final int COLLABORATION_ADDSIGN = 3;

	/**
	 * 转发协同
	 */
	public static final int COLLABORATION_TRANSMIT = 4;

	private static int function = COLLABORATION_NEW;

	/**
	 * 设置此Activity显示方式,涉及到UI不同
	 * @param function COLLABORATION_NEW(1),COLLABORATION_SHOW(2),COLLABORATION_ADDSIGN(3)
	 */
	public static void setFunction(int function) {
		WorkFlowActivity.function = function;
	}

	private static Flow resultData;

	/**
	 * 获取当前流程图显示的结果数据
	 */
	public static Flow getResult() {
		return resultData;
	}

	/**
	 * 获取当前流程图显示的结果数据后要清空。
	 */
	public static void setResultData(Flow resultData) {
		WorkFlowActivity.resultData = resultData;
	}

	private static boolean modify = false;
	private Button helperBnt;
	/**
	 * 标志是否有发送按钮
	 */
	private boolean isHasSendButton;
	private String collaborationID;

	/**
	 * 流程图是否修改过
	 */
	public static boolean hasModify() {
		return modify;
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.NewWorkFlow);
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.NewWorkFlow);
	}
}
