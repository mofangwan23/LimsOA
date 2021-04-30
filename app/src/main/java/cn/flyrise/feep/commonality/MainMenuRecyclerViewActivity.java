package cn.flyrise.feep.commonality;

import static cn.flyrise.feep.core.common.X.Func.Schedule;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import cn.flyrise.android.protocol.model.User;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.utils.ContactsIntent;
import cn.flyrise.feep.collaboration.activity.NewCollaborationActivity;
import cn.flyrise.feep.commonality.adapter.MainMenuRecyclerAdapter;
import cn.flyrise.feep.commonality.bean.MainMenuId;
import cn.flyrise.feep.commonality.bean.MainMenuRecyclerItem;
import cn.flyrise.feep.commonality.manager.XunFeiManager;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.email.NewAndReplyMailActivity;
import cn.flyrise.feep.form.FormListActivity;
import cn.flyrise.feep.knowledge.KnowledgeSearchActivity;
import cn.flyrise.feep.knowledge.PubFileListActivity;
import cn.flyrise.feep.knowledge.RecFileListActivity;
import cn.flyrise.feep.location.SignInMainTabActivity;
import cn.flyrise.feep.location.fragment.SignInLeaderTabStatisFragment;
import cn.flyrise.feep.location.views.SignInCalendarActivity;
import cn.flyrise.feep.location.views.SignInLocusActivity;
import cn.flyrise.feep.location.views.SignInSettingActivity;
import cn.flyrise.feep.schedule.NewScheduleActivity;
import cn.flyrise.feep.utils.Patches;
import cn.flyrise.feep.workplan7.PlanCreateMainActivity;
import cn.flyrise.feep.workplan7.WorkPlanSearchActivity;
import cn.flyrise.feep.workplan7.WorkPlanWaitSendActivity;
import cn.squirtlez.frouter.FRouter;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-7-12.
 */
public class MainMenuRecyclerViewActivity extends BaseActivity {

	public static final String MENU_TYPE = "menu_dialog_type";
	public static final String MAIN_MENU = "main_dialog_menu";//消息主界面
	public static final String ATTENDANCE_MENU = "attendance_dialog_menu";//签到
	public static final String APPROVAL_MENU = "appoval_dialog_menu";//审批
	public static final String WORKPLAN_MENU = "workplan_dialog_menu";//计划
	public static final String KNOWLEDGE_MENU = "knowledge_dialog_menu";//知识
	private RecyclerView recyclerView;
	private RelativeLayout layout;
	private View mTitleView;
	private final int START_DATA = 1001;
	private String currentType;
	private String searchTitleBarName;
	private int searchType;
	public static final String SEARCH_TITLEBAR_NAME = "search_titlebar_name";
	public static final String SEARCH_TYPE = "search_TYPE";
	public static final String WORKPLAN_USERS = "workplan_users";//计划会将人员传过来
	public static final String WORKPLAN_USER_ID = "workplan_user_id";
	public static final int WORKPLAN_USER_ID_CODE = 1001;
	private ArrayList<User> workPlanUsers;
	public static boolean isShowMainMenuRecyclerView = false;
	private float movingDistance = 0;
	private float downY = 0;
	private float moveY = 0;

	private boolean isLocaitonSignTime = false;
	private boolean isLocaitonSignLeader = false;

	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (START_DATA == msg.what) {
				bindDatas();
			}
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu_recyclerview_layout);
	}

	@Override
	public void bindView() {
		super.bindView();
		isShowMainMenuRecyclerView = true;
		mTitleView = this.findViewById(R.id.title_layout);
		layout = this.findViewById(R.id.main_menu_layout);
		recyclerView = this.findViewById(R.id.recyclerview);
		recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
		recyclerView.setEnabled(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !DevicesUtil.isSpecialDevice()) {
			int statusBarHeight = DevicesUtil.getStatusBarHeight(this);
			mTitleView.setPadding(0, statusBarHeight, 0, 0);
		}
	}

	@Override
	public void bindData() {
		super.bindData();
		Intent intent = getIntent();
		if (intent != null) {
			currentType = intent.getStringExtra(MENU_TYPE);
			searchTitleBarName = intent.getStringExtra(SEARCH_TITLEBAR_NAME);
			searchType = intent.getIntExtra(SEARCH_TYPE, -1);
			String workPlanData = intent.getStringExtra(WORKPLAN_USERS);
			isLocaitonSignTime = intent.getBooleanExtra("IS_LOCATION_SIGN_TIME", false);
			isLocaitonSignLeader = intent.getBooleanExtra("IS_LOCATION_SIGN_LEADER", false);
			if (!TextUtils.isEmpty(workPlanData)) {
				workPlanUsers = GsonUtil.getInstance().fromJson(workPlanData, new TypeToken<ArrayList<User>>() {
				}.getType());
			}
		}
		if (TextUtils.isEmpty(currentType)) {
			currentType = MAIN_MENU;
		}
		myHandler.sendEmptyMessageDelayed(START_DATA, 180);
	}

	@Override
	public void bindListener() {
		super.bindListener();
		layout.setOnClickListener(v -> finish());
		if (WORKPLAN_MENU.equals(currentType))
			return;
		recyclerView.setOnTouchListener((v, event) -> {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					downY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					moveY = event.getY();
					break;
				case MotionEvent.ACTION_UP:
					movingDistance = moveY - downY;
					if (movingDistance < 300) {
						finish();
					}
					movingDistance = 0;
					moveY = 0;
					downY = 0;
					break;
				default:
					break;
			}
			return false;
		});
	}

	private void bindDatas() {
		List<MainMenuRecyclerItem> lists = initData();
		MainMenuRecyclerAdapter mAdapter = new MainMenuRecyclerAdapter(this, recyclerView, lists, currentType);
		recyclerView.setAdapter(mAdapter);
		mAdapter.setOnItemClickListener(this::menuClickListener);
	}

	private List<MainMenuRecyclerItem> initData() {
		List<MainMenuRecyclerItem> items = null;
		if (MAIN_MENU.equals(currentType)) {
			items = setMainMenuItems();
		}
		else if (ATTENDANCE_MENU.equals(currentType)) {
			items = setAttendanceMenuItems();
		}
		else if (APPROVAL_MENU.equals(currentType)) {
			items = setApprovalMenuItems();
		}
		else if (WORKPLAN_MENU.equals(currentType)) {
			items = setWorkPlanItems();
		}
		else if (KNOWLEDGE_MENU.equals(currentType)) {
			items = setKnowledgeMenuItems();
		}
		return items == null ? null : items;
	}

	/**
	 * 消息主界面菜单按钮
	 */
	private List<MainMenuRecyclerItem> setMainMenuItems() {
		List<MainMenuRecyclerItem> items = new ArrayList<>();
		// 计划、签到、、、、
		if (FunctionManager.hasPatch(Patches.PATCH_HUANG_XIN)) {
			MainMenuRecyclerItem intiateChat = new MainMenuRecyclerItem();
			intiateChat.setRecyclerItem(MainMenuId.INITIATE_CHAT, getResources().getString(R.string.alertdialog_chat),
					R.drawable.alertdialog_listview_item_chat);
			items.add(intiateChat);
		}
		MainMenuRecyclerItem intiateCollaboration = new MainMenuRecyclerItem();
		intiateCollaboration
				.setRecyclerItem(MainMenuId.INITIATE_COLLABORATION, getResources().getString(R.string.alertdialog_collaboration),
						R.drawable.alertdialog_listview_item_collabortion);
		items.add(intiateCollaboration);
		MainMenuRecyclerItem initiateFlow = new MainMenuRecyclerItem();
		initiateFlow.setRecyclerItem(MainMenuId.INITIATE_FLOW, getResources().getString(R.string.alertdialog_process),
				R.drawable.alertdialog_listview_item_process);
		items.add(initiateFlow);

		if (FunctionManager.hasModule(Func.Location)) {
			MainMenuRecyclerItem signLocation = new MainMenuRecyclerItem();
			signLocation.setRecyclerItem(MainMenuId.SIGN_LOCATION, getResources().getString(R.string.alertdialog_location),
					R.drawable.alertdialog_listview_item_location);
			items.add(signLocation);
		}

		if (FunctionManager.hasModule(Func.Plan)) {
			MainMenuRecyclerItem writePlan = new MainMenuRecyclerItem();
			writePlan.setRecyclerItem(MainMenuId.WRITE_PLAN, getResources().getString(R.string.alertdialog_workplan),
					R.drawable.alertdialog_listview_item_workplan);
			items.add(writePlan);
		}

		if (FunctionManager.hasModule(Func.Schedule)) {
			MainMenuRecyclerItem writeAgenda = new MainMenuRecyclerItem();
			writeAgenda.setRecyclerItem(MainMenuId.WRITE_AGENDA, getResources().getString(R.string.alertdialog_schedule),
					R.drawable.alertdialog_listview_item_schedule);
			items.add(writeAgenda);
		}

		if (FunctionManager.hasModule(Func.Mail)) {
			MainMenuRecyclerItem writeEmail = new MainMenuRecyclerItem();
			writeEmail.setRecyclerItem(MainMenuId.WRITE_EMAIL, getResources().getString(R.string.alertdialog_email),
					R.drawable.alertdialog_listview_item_mail);
			items.add(writeEmail);
		}

		if (FunctionManager.hasPatch(Patches.PATCH_ROBOT_UNDERSTANDER)) {
			MainMenuRecyclerItem writeRobot = new MainMenuRecyclerItem();
			writeRobot.setRecyclerItem(MainMenuId.ROBOT_UNDERSTANDER, getResources().getString(R.string.alertdialog_robot),
					R.drawable.alertdialog_listview_item_voice);
			items.add(writeRobot);
		}

		return items;
	}

	/**
	 * 考勤轨迹菜单按钮
	 */
	private List<MainMenuRecyclerItem> setAttendanceMenuItems() {
		List<MainMenuRecyclerItem> items = new ArrayList<>();
		if (isLocaitonSignLeader || isLocaitonSignTime) {
			MainMenuRecyclerItem attendanceMark = new MainMenuRecyclerItem();
			attendanceMark.setRecyclerItem(MainMenuId.ATTENDANCE_MARK, getResources().getString(R.string.location_locus),
					R.drawable.attendance_mark_icon);
			items.add(attendanceMark);
		}
//		MainMenuRecyclerItem attendanceRecord = new MainMenuRecyclerItem();
//		attendanceRecord.setRecyclerItem(MainMenuId.ATTENDANCE_RECORD, getResources().getString(R.string.location_history_text),
//				R.drawable.attendance_record_icon);
//		items.add(attendanceRecord);
//		if (isLocaitonSignLeader && FunctionManager.hasPatch(Patches.PATCH_SIGN_IN_STATICS)) {
//			MainMenuRecyclerItem attendanceLeaderKanban = new MainMenuRecyclerItem();
//			attendanceLeaderKanban
//					.setRecyclerItem(MainMenuId.LOCATION_LEADER_KANBAN, getResources().getString(R.string.location_leadar_kanban),
//							R.drawable.attendance_record_icon);
//			items.add(attendanceLeaderKanban);
//		}
//		if (isLocaitonSignTime) {
		MainMenuRecyclerItem attendanceSetting = new MainMenuRecyclerItem();
		attendanceSetting.setRecyclerItem(MainMenuId.ATTENDANCE_SETTING, getResources().getString(R.string.location_setting),
				R.drawable.attendance_setting_icon);
		items.add(attendanceSetting);
//		}
		return items;
	}

	/**
	 * 审批界面菜单按钮
	 */
	private List<MainMenuRecyclerItem> setApprovalMenuItems() {
		List<MainMenuRecyclerItem> items = new ArrayList<>();
//		MainMenuRecyclerItem searchMenu = new MainMenuRecyclerItem();
//		searchMenu.setRecyclerItem(MainMenuId.SEARCH_MENU, getResources().getString(R.string.approval_search),
//				R.drawable.alertdialog_search_icon);
//		items.add(searchMenu);
		MainMenuRecyclerItem intiateCollaboration = new MainMenuRecyclerItem();
		intiateCollaboration.setRecyclerItem(MainMenuId.INITIATE_COLLABORATION, getResources().getString(R.string.approval_new),
				R.drawable.alertdialog_listview_item_collabortion);
		items.add(intiateCollaboration);
		if (FunctionManager.hasModule(Func.NewForm)) {
			MainMenuRecyclerItem initiateFlow = new MainMenuRecyclerItem();
			initiateFlow.setRecyclerItem(MainMenuId.INITIATE_FLOW, getResources().getString(R.string.approval_from),
					R.drawable.alertdialog_listview_item_process);
			items.add(initiateFlow);
		}
		return items;
	}


	/**
	 * 知识中心界面菜单按钮
	 */
	private List<MainMenuRecyclerItem> setKnowledgeMenuItems() {
		List<MainMenuRecyclerItem> items = new ArrayList<>();
		MainMenuRecyclerItem searchMenu = new MainMenuRecyclerItem();
		searchMenu.setRecyclerItem(MainMenuId.KNOWLEDGE_SEARCH, getResources().getString(R.string.approval_search),
				R.drawable.alertdialog_search_icon);
		items.add(searchMenu);
		MainMenuRecyclerItem intiatePublic = new MainMenuRecyclerItem();
		intiatePublic.setRecyclerItem(MainMenuId.KNOWLEDGE_PUBLIC, getString(R.string.know_published),
				R.drawable.alertdialog_listview_item_public);
		items.add(intiatePublic);
		MainMenuRecyclerItem intiateReceiver = new MainMenuRecyclerItem();
		intiateReceiver.setRecyclerItem(MainMenuId.KNOWLEDGE_RECEIVER, getString(R.string.know_receive),
				R.drawable.alertdialog_listview_item_receiver);
		items.add(intiateReceiver);
		return items;
	}


	/**
	 * 计划弹出框
	 */
	private List<MainMenuRecyclerItem> setWorkPlanItems() {
		List<MainMenuRecyclerItem> items = new ArrayList<>();
		MainMenuRecyclerItem searchMenu = new MainMenuRecyclerItem();
		searchMenu.setRecyclerItem(MainMenuId.WORKPLAN_SEARCG, getResources().getString(R.string.approval_search),
				R.drawable.alertdialog_search_icon);
		items.add(searchMenu);
		MainMenuRecyclerItem writePlan = new MainMenuRecyclerItem();
		writePlan.setRecyclerItem(MainMenuId.WRITE_PLAN, getResources().getString(R.string.alertdialog_workplan),
				R.drawable.alertdialog_listview_item_workplan);
		items.add(writePlan);
		MainMenuRecyclerItem waitSend = new MainMenuRecyclerItem();
		waitSend.setRecyclerItem(MainMenuId.WORKPLAN_WAIT_SEND, getResources().getString(R.string.committed),
				R.drawable.alertdialog_listview_item_collabortion);
		items.add(waitSend);
		if (workPlanUsers != null) {
			for (User item : workPlanUsers) {
				if (item == null || TextUtils.isEmpty(item.getId())) {
					continue;
				}
				MainMenuRecyclerItem workPlanUser = new MainMenuRecyclerItem();
				AddressBook userInfo = CoreZygote.getAddressBookServices().queryUserInfo(item.getId());

				workPlanUser.setRecyclerItem(MainMenuId.WRITE_EMAIL_USER,
						userInfo == null ? "" : userInfo.name,
						R.drawable.alertdialog_listview_item_workplan, item.getId());
				items.add(workPlanUser);
			}
		}

		return items;
	}

	@Override
	protected void onResume() {
		super.onResume();
		overridePendingTransition(0, R.anim.alert_dialog_in);
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.MainMenuRecyclerViewActivity);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.MainMenuRecyclerViewActivity);
		isShowMainMenuRecyclerView = false;
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.alert_dialog_out);
	}

	private void menuClickListener(MainMenuRecyclerItem clickedMenu) {
		int menuId = clickedMenu.menuId;
		String workPlanUserId = clickedMenu.userId;
		switch (menuId) {
			case MainMenuId.INITIATE_CHAT:
				new ContactsIntent(MainMenuRecyclerViewActivity.this)
						.title(CommonUtil.getString(R.string.lbl_message_title_chat)).startChat().open();
				break;
			case MainMenuId.INITIATE_COLLABORATION:
				Intent intentNewCollaboration = new Intent(MainMenuRecyclerViewActivity.this, NewCollaborationActivity.class);
//                if (currentType.equals(APPROVAL_MENU)) {
//                    intentNewCollaboration.putExtra(NewCollaborationActivity.NEW_PAGELISTACTIVITY, "true");
//                }
				startActivity(intentNewCollaboration);
				break;
			case MainMenuId.INITIATE_FLOW:
				startActivityNewForm();
				break;
			case MainMenuId.SIGN_LOCATION:
				FePermissions.with(MainMenuRecyclerViewActivity.this)
						.requestCode(PermissionCode.LOCATION)
						.rationaleMessage(getResources().getString(R.string.permission_rationale_location))
						.permissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})
						.request();
				return;
			case MainMenuId.WRITE_PLAN:
				startActivity(new Intent(MainMenuRecyclerViewActivity.this, PlanCreateMainActivity.class));
				break;
			case MainMenuId.WORKPLAN_WAIT_SEND:
				startActivity(new Intent(MainMenuRecyclerViewActivity.this, WorkPlanWaitSendActivity.class));
				break;
			case MainMenuId.WORKPLAN_SEARCG:
				startActivity(new Intent(MainMenuRecyclerViewActivity.this, WorkPlanSearchActivity.class));
				break;
			case MainMenuId.WRITE_AGENDA:
				if (FunctionManager.isNative(Schedule)) {
					startActivity(new Intent(MainMenuRecyclerViewActivity.this, NewScheduleActivity.class));
				}
				else {

//					Intent intent = new Intent(MainMenuRecyclerViewActivity.this, ScheduleActivity.class);
//					CordovaShowInfo showInfo = new CordovaShowInfo();
//					showInfo.type = Func.Schedule;
//					showInfo.pageid = CordovaShowUtils.ADD_SCHEDULE;
//					intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, GsonUtil.getInstance().toJson(showInfo));
//					startActivity(intent);

					FRouter.build(MainMenuRecyclerViewActivity.this, "/x5/browser")
							.withInt("moduleId", Func.Schedule)
							.withString("pageid", "schedule")
							.go();
				}
				break;
			case MainMenuId.WRITE_EMAIL:
				startActivity(new Intent(MainMenuRecyclerViewActivity.this, NewAndReplyMailActivity.class));
				break;
			case MainMenuId.ATTENDANCE_MARK:
				startActivity(new Intent(MainMenuRecyclerViewActivity.this, SignInLocusActivity.class));
				break;
			case MainMenuId.ATTENDANCE_RECORD:
				startActivity(new Intent(MainMenuRecyclerViewActivity.this, SignInCalendarActivity.class));
				break;
			case MainMenuId.ATTENDANCE_SETTING:
				Intent intent = new Intent(MainMenuRecyclerViewActivity.this, SignInSettingActivity.class);
				intent.putExtra("IS_LOCATION_SIGN_TIME", isLocaitonSignTime);
				startActivity(intent);
				break;
			case MainMenuId.SEARCH_MENU:
				Intent searchIntent = new Intent(MainMenuRecyclerViewActivity.this, MessageSearchActivity.class);
				searchIntent.putExtra(MessageSearchActivity.REQUESTTYPE, searchType);
				searchIntent.putExtra(MessageSearchActivity.REQUESTNAME, searchTitleBarName);
				startActivity(searchIntent);
				break;
			case MainMenuId.WRITE_EMAIL_USER:
				Intent workPlanIntent = new Intent();
				workPlanIntent.putExtra(MainMenuRecyclerViewActivity.WORKPLAN_USER_ID, workPlanUserId);
				MainMenuRecyclerViewActivity.this.setResult(MainMenuRecyclerViewActivity.WORKPLAN_USER_ID_CODE, workPlanIntent);
				break;
			case MainMenuId.KNOWLEDGE_RECEIVER:
				Intent receiverIntent = new Intent(this, RecFileListActivity.class);
				startActivity(receiverIntent);
				break;
			case MainMenuId.KNOWLEDGE_SEARCH:
				KnowledgeSearchActivity.StartSearchListActivity(this, searchType);
				break;
			case MainMenuId.KNOWLEDGE_PUBLIC:
				Intent knowledgePublishedIntent = new Intent(this, PubFileListActivity.class);
				startActivity(knowledgePublishedIntent);
				break;
			case MainMenuId.ROBOT_UNDERSTANDER:
				XunFeiManager.startRobot(this);
				break;
			case MainMenuId.LOCATION_LEADER_KANBAN:
				startActivity(new Intent(this, SignInLeaderTabStatisFragment.class));
				break;
			default:
				break;
		}
		FELog.i("mainmenu", "-->>>>Mainmenu:" + menuId);
		finish();
	}

	private void startActivityNewForm() {
		Module module = FunctionManager.findModule(Func.NewForm);
		if (module != null && !TextUtils.isEmpty(module.url)) {
			MobclickAgent.onEvent(this, "6050005");
			FRouter.build(this, "/x5/browser")
					.withString("appointURL", module.url)
					.withInt("moduleId", Func.Default)
					.go();
		}
		else {
			MobclickAgent.onEvent(this, "6050004");
			startActivity(new Intent(MainMenuRecyclerViewActivity.this, FormListActivity.class));
		}
	}

	@PermissionGranted(PermissionCode.LOCATION)
	public void onLocationPermissionGranted() {
		startActivity(new Intent(MainMenuRecyclerViewActivity.this, SignInMainTabActivity.class));
		finish();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}
}
