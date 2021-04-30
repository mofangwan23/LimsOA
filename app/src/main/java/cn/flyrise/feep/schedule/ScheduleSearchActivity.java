package cn.flyrise.feep.schedule;

import static cn.flyrise.feep.core.common.X.RequestType.Meeting;

import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.schedule.AgendaResponseItem;
import cn.flyrise.feep.core.base.component.FESearchListActivity;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.squirtlez.frouter.FRouter;
import cn.squirtlez.frouter.annotations.Route;

/**
 * @author ZYP
 * @since 2018-05-10 20:42
 */
@Route("/schedule/search")
public class ScheduleSearchActivity extends FESearchListActivity<AgendaResponseItem> {

	private ScheduleSearchAdapter mAdapter;
	private ScheduleSearchPresenter mPresenter;

	@Override
	public void bindView() {
		super.bindView();
		this.listView.setCanRefresh(false);
	}

	@Override
	public void bindData() {
		et_Search.setHint("搜索日程...");
		searchKey = getIntent().getStringExtra("keyword");

		mAdapter = new ScheduleSearchAdapter();
		mPresenter = new ScheduleSearchPresenter(this);
		setAdapter(mAdapter);
		setPresenter(mPresenter);

		if (!TextUtils.isEmpty(searchKey)) {
			et_Search.setText(searchKey);
			et_Search.setSelection(searchKey.length());
			myHandler.post(searchRunnable);
		}
		else {
			myHandler.postDelayed(() -> DevicesUtil.showKeyboard(et_Search), 500);
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mAdapter.setOnItemClickListener((view, object) -> {
			AgendaResponseItem item = (AgendaResponseItem) object;
			if (TextUtils.isEmpty(item.meetingId)) {
				FRouter.build(ScheduleSearchActivity.this, "/schedule/detail")
						.withString("EXTRA_SCHEDULE_ID", item.id)
						.withString("EXTRA_EVENT_SOURCE", item.eventSource)
						.withString("EXTRA_EVENT_SOURCE_ID", item.eventSourceId)
						.go();
				return;
			}

			FRouter.build(ScheduleSearchActivity.this, "/particular/detail")
					.withInt("extra_particular_type", 3)
					.withString("extra_business_id", item.meetingId)
					.withSerializable("extra_request_type", Meeting)
					.go();
		});
	}

}
