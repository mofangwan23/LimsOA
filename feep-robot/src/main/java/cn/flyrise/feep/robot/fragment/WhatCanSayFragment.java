package cn.flyrise.feep.robot.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.adapter.WhatCanSayItemAdapter;
import cn.flyrise.feep.robot.analysis.WhatCanSayAnalysis;
import cn.flyrise.feep.robot.entity.WhatCanSayItem;
import cn.flyrise.feep.robot.event.EventMoreDetail;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * 新建：陈冕;
 * 日期： 2017-12-6-14:29.
 */

public class WhatCanSayFragment extends Fragment implements WhatCanSayItemAdapter.OnMoresListener {

	private OnClickeMoreListener listener;

	private RecyclerView mRecyclerView;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.robot_more_layout, container, false);
		mRecyclerView = view.findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		bindData();
	}

	private void bindData() {
		List<WhatCanSayItem> canSayItems = new WhatCanSayAnalysis(getActivity()).analysis();
		List<WhatCanSayItem> items = new ArrayList<>();
		for (WhatCanSayItem item : canSayItems) {
			if (item.moduleId == -1 || FunctionManager.hasModule(item.moduleId)) {
				if (item.moduleId == 5 && !FunctionManager.hasModule(6)) {//新闻公告同事存在才显示
					continue;
				}
				items.add(item);
			}
		}
		WhatCanSayItemAdapter mAdapter = new WhatCanSayItemAdapter(items, this);
		mRecyclerView.setAdapter(mAdapter);
	}

	@Override
	public void more(String title, List<String> mores) {
		if (listener == null || CommonUtil.isEmptyList(mores)) return;
		EventMoreDetail eventMoreDetail = new EventMoreDetail();
		eventMoreDetail.title = title;
		eventMoreDetail.mores = mores;
		listener.onClickeMore(eventMoreDetail);
		EventBus.getDefault().post(eventMoreDetail);
	}

	public void setOnClickMoreListener(OnClickeMoreListener listener) {
		this.listener = listener;
	}

	public interface OnClickeMoreListener {

		void onClickeMore(EventMoreDetail eventMoreDetail);
	}
}
