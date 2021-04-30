package cn.flyrise.feep.robot.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.adapter.MoreDetailAdapter;
import cn.flyrise.feep.robot.event.EventMoreDetail;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 新建：陈冕;
 * 日期： 2017-12-6-14:29.
 * 用户可以说的语句
 */

@SuppressLint("ValidFragment")
public class MoreDetailFragment extends Fragment {

	private TextView mTvTitle;

	private LinearLayout mLayoutHead;

	private MoreDetailAdapter mAdapter;

	private RecyclerView mRecyclerView;

	private OnClickeCancleListener listener;

	private EventMoreDetail moreDetail;

	MoreDetailAdapter.OnClickeItemListener mMoreDetailListener;

	@SuppressLint("ValidFragment")
	public MoreDetailFragment(MoreDetailAdapter.OnClickeItemListener mListener) {
		this.mMoreDetailListener = mListener;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		EventBus.getDefault().register(this);
		View view = inflater.inflate(R.layout.robot_more_fragment_layout, container, false);
		mRecyclerView = view.findViewById(R.id.list_view);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mTvTitle = view.findViewById(R.id.title);
		mLayoutHead = view.findViewById(R.id.head_layout);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		bindData();
		setListener();
	}

	private void bindData() {
		mAdapter = new MoreDetailAdapter(mMoreDetailListener);
		mRecyclerView.setAdapter(mAdapter);
		if (moreDetail != null) {
			mTvTitle.setText(moreDetail.title);
			mAdapter.setData(moreDetail.mores);
		}
	}

	private void setListener() {
		mLayoutHead.setOnClickListener(v -> {
			if (listener != null) {
				listener.onClickeCancle();
			}
		});
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void eventMoreDetail(EventMoreDetail moreDetail) {
		mTvTitle.setText(moreDetail.title);
		if (CommonUtil.isEmptyList(moreDetail.mores)) {
			return;
		}
		mAdapter.setData(moreDetail.mores);
	}

	public void setMoreDetail(EventMoreDetail eventMoreDetail) {
		this.moreDetail = eventMoreDetail;
	}

	public void setOnClickeCancleListener(OnClickeCancleListener listener) {
		this.listener = listener;
	}

	public interface OnClickeCancleListener {

		void onClickeCancle();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
}
