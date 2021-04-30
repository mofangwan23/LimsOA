package cn.flyrise.feep.collaboration.matter;


import android.os.Parcelable;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.matter.adpater.MatterSearchListAdapter;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.collaboration.matter.model.MatterEvent;
import cn.flyrise.feep.collaboration.matter.presenter.MatterListPresenter;
import cn.flyrise.feep.core.base.component.FESearchListActivity;

/**
 * Created by klc on 2017/5/12.
 * 选择关联事项列表
 */
public class MatterSearchListActivity extends FESearchListActivity<Matter> {

	private MatterSearchListAdapter mAdapter;
	private int searchType;
	private List<Matter> selectedAssociations;

	@Override
	public void bindData() {
		et_Search.setHint(getResources().getString(R.string.search) + "...");

		mAdapter = new MatterSearchListAdapter();
		Parcelable[] parcelables = getIntent().getParcelableArrayExtra("associations");
		Matter[] associations = Arrays.copyOf(parcelables, parcelables.length, Matter[].class);
		selectedAssociations = new ArrayList<>();
		Collections.addAll(selectedAssociations, associations);
		mAdapter.setSelectAssociations(selectedAssociations);
		setAdapter(mAdapter);

		searchType = getIntent().getIntExtra("type", searchType);
		MatterListPresenter mPresenter;
		if (searchType == MatterListActivity.MATTER_KNOWLEDGE) {
			String folderID = getIntent().getStringExtra("folderID");
			String attr = getIntent().getStringExtra("attr");
			mPresenter = new MatterListPresenter(this, searchType, folderID, attr);
		}
		else {
			mPresenter = new MatterListPresenter(this, searchType);
		}
		setPresenter(mPresenter);
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mAdapter.setOnItemClickListener((view, object) -> {
			ImageView checkBox = (ImageView) view;
			Matter association = (Matter) object;
			if (selectedAssociations.contains(association)) {
				selectedAssociations.remove(association);
				checkBox.setImageResource(R.drawable.no_select_check);
				MatterEvent associationEvent = new MatterEvent(1, association);
				EventBus.getDefault().post(associationEvent);
			}
			else {
				selectedAssociations.add(association);
				checkBox.setImageResource(R.drawable.node_current_icon);
				MatterEvent associationEvent = new MatterEvent(0, association);
				EventBus.getDefault().post(associationEvent);
			}
		});
	}

}
