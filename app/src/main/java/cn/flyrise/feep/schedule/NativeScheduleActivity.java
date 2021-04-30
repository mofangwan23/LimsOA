package cn.flyrise.feep.schedule;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import cn.flyrise.android.protocol.entity.schedule.AgendaResponseItem;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.schedule;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.LanguageManager;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.event.EventRefresh;
import cn.flyrise.feep.meeting7.ui.MeetingDetailActivity;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.particular.ParticularIntent;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;
import cn.flyrise.feep.schedule.utils.ScheduleUtil;
import cn.flyrise.feep.schedule.view.NativeScheduleAdapter;
import cn.flyrise.feep.schedule.view.NativeScheduleListView;
import cn.flyrise.feep.schedule.view.NativeScheduleScrollView;
import cn.flyrise.feep.utils.Patches;
import cn.squirtlez.frouter.annotations.Route;
import com.haibuzou.datepicker.calendar.cons.DPMode;
import com.haibuzou.datepicker.calendar.views.MonthView;
import com.haibuzou.datepicker.calendar.views.WeekView;
import java.util.Calendar;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author ZYP
 * @since 2016-11-29 11:28
 */
@Route("/schedule/native")
public class NativeScheduleActivity extends NotTranslucentBarActivity implements NativeScheduleContract.IView {

	private final Handler mHandler = new Handler();
	private FEToolbar mToolBar;
	private WeekView mWeekView;
	private MonthView mMonthView;

	private TextView mTvDate;
	private TextView mTvWeek;
	private FELoadingDialog mLoadingDialog;

	private NativeScheduleAdapter mAdapter;
	private NativeScheduleListView mListView;
	private NativeScheduleScrollView mScrollView;
	private NativeScheduleContract.IPresenter mPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		mPresenter = new NativeSchedulePresenter(this);
		setContentView(R.layout.activity_native_schedule);
		mHandler.postDelayed(() -> {
			View view = findViewById(R.id.layoutScheduleWeekNav);
			int navViewHeight = view.getMeasuredHeight();
			int toolBarHeight = mToolBar.getMeasuredHeight() + PixelUtil.dipToPx(30);
			int weekViewHeight = mWeekView.getMeasuredHeight();
			int displayHeight = getResources().getDisplayMetrics().heightPixels;
			int minHeight = displayHeight - toolBarHeight - navViewHeight - weekViewHeight;
			findViewById(R.id.layoutScheduleList).setMinimumHeight(minHeight);
		}, 500);
		mPresenter.start();
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		this.mToolBar = toolbar;
		this.mToolBar.setLineVisibility(View.GONE);
		Calendar calendar = Calendar.getInstance();
		this.mToolBar.setRightText(R.string.schedule_main_right);
		this.mToolBar.setRightTextClickListener(this::gotoTodaySchedule);
		this.mToolBar.setTitle(calendar.get(Calendar.YEAR) + "." + (calendar.get(Calendar.MONTH) + 1));
	}

	@Override
	public void bindView() {
		mTvWeek = findViewById(R.id.tvWeek);
		mTvDate = findViewById(R.id.tvDate);
		mWeekView = findViewById(R.id.weekView);
		mMonthView = findViewById(R.id.monthView);
		mListView = findViewById(R.id.listView);
		mScrollView = findViewById(R.id.nativeScheduleScrollView);
		mScrollView.setWeekView(mWeekView);
		mScrollView.setShadowView(findViewById(R.id.shadowView));
		if (LanguageManager.isChinese()) {
			mWeekView.setFestivalDisplay(true);
			mMonthView.setFestivalDisplay(true);
		}
		else {
			mWeekView.setFestivalDisplay(false);
			mMonthView.setFestivalDisplay(false);
		}
	}

	@Override
	public void bindData() {
		Drawable drawable = getResources().getDrawable(R.drawable.add_btn);
		drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		ScheduleUtil.reset();

		mMonthView.setDPMode(DPMode.SINGLE);
		mMonthView.reset(year, month, day);

		mWeekView.setDPMode(DPMode.SINGLE);
		mWeekView.reset(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH), 0);

//        int minHeight = (int) (getResources().getDisplayMetrics().heightPixels * 3.6F / 5);
//        findViewById(R.id.layoutScheduleList).setMinimumHeight(minHeight);

		mListView.setAdapter(mAdapter = new NativeScheduleAdapter());
		mListView.setEmptyView(findViewById(R.id.emptyView));
	}

	@SuppressLint("ClickableViewAccessibility") @Override
	public void bindListener() {
		mMonthView.setDateChangeListener((year, month) -> {
			if (mToolBar != null) {
				mToolBar.setTitle(year + "." + month);
			}
		});

		mMonthView.setOnTouchListener(new View.OnTouchListener() {
			private float iLastX;
			private float iLastY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float currX = event.getX();
				float currY = event.getY();

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					iLastX = currX;
					iLastY = currY;
				}
				else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (Math.abs(currX - iLastX) > Math.abs(currY - iLastY)) {
						mScrollView.requestDisallowInterceptTouchEvent(true);
					}
				}
				else if (event.getAction() == MotionEvent.ACTION_UP) {
					mScrollView.requestDisallowInterceptTouchEvent(false);
				}
				return false;
			}
		});

		findViewById(R.id.floatingActionButton).setOnClickListener(view -> {
			NewScheduleActivity.startActivity(NativeScheduleActivity.this, mPresenter.getSeletedDate());
		});

		mWeekView.setDatePickedListener(mPresenter::requestSchedule);
		mMonthView.setDatePickedListener(mPresenter::requestSchedule);
		mListView.setOnItemClickListener(this::onItemClick);
	}

	private void gotoTodaySchedule(View view) {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int week = calendar.get(Calendar.WEEK_OF_MONTH);

		ScheduleUtil.reset();

		mMonthView.resetBG(week - 1);
		mMonthView.isUseCache = false;
		mMonthView.reset(year, month, day);

		mWeekView.resetMove();
		mWeekView.isUseCache = false;
		mWeekView.reset(year, month, day, mMonthView.offsetX);
		mMonthView.offsetX = 0;

		mWeekView.cirApr.clear();
		mWeekView.cirDpr.clear();
		mMonthView.cirApr.clear();
		mMonthView.cirDpr.clear();

		mPresenter.requestSchedule(year + "." + month + "." + day);
	}

	private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AgendaResponseItem item = (AgendaResponseItem) mAdapter.getItem(position);
		if (TextUtils.isEmpty(item.meetingId)) {
			ScheduleDetailActivity.startActivity(this, item.eventSourceId, item.eventSource, item.id);
		}
		else {
			if (FunctionManager.hasPatch(Patches.PATCH_MEETING_MANAGER)) {
				Intent intent = new Intent(NativeScheduleActivity.this, MeetingDetailActivity.class);
				intent.putExtra("meetingId", item.meetingId);
				intent.putExtra("meetingType", -1);
				startActivity(intent);
			}
			else {
				new ParticularIntent.Builder(this)
						.setTargetClass(ParticularActivity.class)
						.setParticularType(ParticularPresenter.PARTICULAR_MEETING)
						.setBusinessId(item.meetingId)
						.setRequestCode(0)
						.create()
						.start();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		mHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void showLoading() {
		if (mLoadingDialog == null) {
			mLoadingDialog = new FELoadingDialog.Builder(this).setCancelable(false).create();
		}
		mLoadingDialog.show();
	}

	@Override
	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
		}
		mLoadingDialog = null;
	}

	@Override
	public void displayCurrentDate(String date, String week) {
		mTvWeek.setText(week);
		mTvDate.setText(date);
	}

	@Override
	public void displayScheduleList(List<AgendaResponseItem> scheduleItems) {
		mAdapter.setScheduleItems(scheduleItems);
	}

	@Override
	public void displayAgendaPromptInMonthView(List<Integer> promptLists) {
		mMonthView.resetHasTaskDayList();
		if (CommonUtil.isEmptyList(promptLists)) {
			return;
		}
		mMonthView.getHasTaskDayList().addAll(promptLists);
		mMonthView.updateTaskRemind();
		mWeekView.postInvalidate();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == K.schedule.detail_request_code
				&& resultCode == K.schedule.detail_result_code && data != null) {   // 删除日程，部分刷新
			String scheduleId = data.getStringExtra(K.schedule.schedule_id);
			if (TextUtils.isEmpty(scheduleId)) {
				return;
			}
			String eventSourceId = mAdapter.removeSchedule(scheduleId);
			mPresenter.removeSchedule(eventSourceId);
		}
		else if (requestCode == K.schedule.detail_request_code && resultCode == K.schedule.share_result_code) { // 分享完日程，强制刷新
			mPresenter.forceRefresh();
			mPresenter.requestSchedule(mPresenter.getSeletedDate());
		}
		else if (requestCode == K.schedule.new_request_code && resultCode == K.schedule.new_result_code) {  // 新建完日程，强制刷新
			mPresenter.forceRefresh();
			mPresenter.requestSchedule(mPresenter.getSeletedDate());
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void forceRefreshWhenScheduleUpdate(EventRefresh refreshEvent) {
		if (refreshEvent.code == schedule.modify_schedule_code) {
			if (mPresenter != null) {
				mPresenter.forceRefresh();
				mPresenter.requestSchedule(mPresenter.getSeletedDate());
			}
		}
	}
}
