package cn.flyrise.feep.robot.adapter.holder;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.adapter.TrainSubAdapter;
import cn.flyrise.feep.robot.entity.RobotTrainItem;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-12-5-10:23.
 */

public class TrainViewHodler extends RobotViewHodler {

	private TextView mTvTrainText;

	private RecyclerView mRecyclerView;

	public TrainViewHodler(View itemView, Context mContext) {
		super(itemView);
		mTvTrainText = itemView.findViewById(R.id.train_text);
		mRecyclerView = itemView.findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
	}

	public void setTrainViewHodler() {
		List<RobotTrainItem> trainItems = item.trainItems;
		if (CommonUtil.isEmptyList(trainItems)) return;
		mTvTrainText.setText(item.content);
		mRecyclerView.setAdapter(new TrainSubAdapter(trainItems));
	}

	@Override
	public void onDestroy() {

	}
}
