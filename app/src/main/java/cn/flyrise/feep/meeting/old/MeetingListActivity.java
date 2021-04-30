package cn.flyrise.feep.meeting.old;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.meeting.MeetingSearchActivity;
//import cn.flyrise.feep.meeting7.ui.MeetingManagerActivity;
import cn.flyrise.feep.meeting7.ui.MeetingManagerActivity;
import cn.flyrise.feep.utils.Patches;
import com.dk.view.badge.BadgeUtil;

import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.FEListActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.particular.ParticularIntent;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;

public class MeetingListActivity extends FEListActivity<MeetingListItemBean> {

	private MeetingListAdapter mMeetingAdapter;
	private MeetingListItemBean clickItem;

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.meeting_manager);
	}

	@Override
	public void bindView() {
		super.bindView();
//		tv_search.setText(getResources().getString(R.string.meeting_search_title));
		listView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
	}

	@Override
	public void bindData() {
		super.bindData();
		mMeetingAdapter = new MeetingListAdapter();
		setAdapter(mMeetingAdapter);
		setPresenter(new MeetingListPresenter(this));
		startLoadData();
	}

	@Override
	public void bindListener() {
		super.bindListener();
		layoutSearch.setOnClickListener(v -> startActivity(new Intent(MeetingListActivity.this, MeetingSearchActivity.class)));
		mMeetingAdapter.setOnItemClickListener((view, object) -> {
			clickItem = (MeetingListItemBean) object;
			if (clickItem == null) {
				return;
			}
			if (clickItem.isNews()) {
				FEApplication feApplication = (FEApplication) this.getApplicationContext();
				int num = feApplication.getCornerNum() - 1;
				BadgeUtil.setBadgeCount(this, num);//角标
				feApplication.setCornerNum(num);
			}
			new ParticularIntent.Builder(MeetingListActivity.this)
					.setTargetClass(ParticularActivity.class)
					.setParticularType(ParticularPresenter.PARTICULAR_MEETING)
					.setBusinessId(clickItem.getId())
					.setRequestCode(0)
					.create()
					.start();
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			clickItem.setStatus(data.getStringExtra("status"));
			mMeetingAdapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.MeetingList);
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.MeetingList);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
