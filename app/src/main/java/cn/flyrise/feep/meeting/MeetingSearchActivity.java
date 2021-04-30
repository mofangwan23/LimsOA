package cn.flyrise.feep.meeting;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.FESearchListActivity;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.meeting.old.MeetingListAdapter;
import cn.flyrise.feep.meeting.old.MeetingListItemBean;
import cn.flyrise.feep.meeting.old.MeetingListPresenter;
import cn.flyrise.feep.meeting7.ui.MeetingDetailActivity;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.particular.ParticularIntent;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;
import cn.flyrise.feep.utils.Patches;
import cn.squirtlez.frouter.annotations.Route;

/**
 * 陈冕
 * 功能：会议搜索
 * Created by Administrator on 2016-3-17.
 */
@Route("/meeting/search")
public class MeetingSearchActivity extends FESearchListActivity<MeetingListItemBean> {

	private MeetingListAdapter mMeetingAdapter;
	private MeetingListItemBean mClickItem;

	@Override
	public void bindData() {
		et_Search.setHint(getResources().getString(R.string.meeting_search_title) + "...");
		mMeetingAdapter = new MeetingListAdapter();
		listView.setAdapter(mMeetingAdapter);
		setAdapter(mMeetingAdapter);
		setPresenter(new MeetingListPresenter(this));

		String keyword = getIntent().getStringExtra("keyword");
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
		mMeetingAdapter.setOnItemClickListener((view, object) -> {
			View focusView = getCurrentFocus();
			if (view != null) {
				DevicesUtil.hideKeyboard(focusView);
			}
			mClickItem = (MeetingListItemBean) object;
			if (FunctionManager.hasPatch(Patches.PATCH_MEETING_MANAGER)) {
				Intent intent = new Intent(this, MeetingDetailActivity.class);
				intent.putExtra("meetingId", mClickItem.getId());
				startActivity(intent);
			}
			else {
				new ParticularIntent.Builder(MeetingSearchActivity.this)
						.setTargetClass(ParticularActivity.class)
						.setParticularType(ParticularPresenter.PARTICULAR_MEETING)
						.setBusinessId(mClickItem.getId())
						.setRequestCode(0)
						.create()
						.start();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			mClickItem.setStatus(data.getStringExtra("status"));
			mMeetingAdapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
