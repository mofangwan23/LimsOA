package cn.flyrise.feep.robot.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.entity.RobotTrainItem;
import cn.flyrise.feep.robot.entity.RobotTrainPrice;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-12-5-10:40.
 * 车票列表
 */

public class TrainSubAdapter extends RecyclerView.Adapter<TrainSubAdapter.TrainItemViewHodler> {

    private List<RobotTrainItem> robotTrainItems;

    public TrainSubAdapter(List<RobotTrainItem> robotTrainItems) {
        this.robotTrainItems = robotTrainItems;
    }

    @Override
    public TrainItemViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TrainItemViewHodler(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.robot_train_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(TrainItemViewHodler holder, int position) {
        if (robotTrainItems == null) {
            return;
        }
        RobotTrainItem trainItem = robotTrainItems.get(position);
        if (trainItem == null) {
            return;
        }
        holder.mTvStartTime.setText(trainItem.startTime);
        holder.mTvEndTime.setText(trainItem.arrivalTime);
        holder.mTvStartStation.setText(trainItem.originStation);
        holder.mTvEndStation.setText(trainItem.terminalStation);
        holder.mTvAllTime.setText(trainItem.runTime);
        holder.mTvTrainNo.setText(trainItem.trainNo);

        if (robotTrainItems.size() == position + 1) {
            holder.mLine.setVisibility(View.GONE);
        }

        List<RobotTrainPrice> trainPrices = trainItem.prices;
        if (CommonUtil.isEmptyList(trainPrices)) {
            return;
        }
        int index = 0;
        String text;
        for (RobotTrainPrice trainPrice : trainPrices) {
            text = trainPrice.name + "￥" + trainPrice.vaule + "元";
            if (index == 0) {
                holder.mTvLowerPrice.setText(text);
            } else if (index == 1) {
                holder.mTvSecondaryPrice.setText(text);
            } else if (index == 2) {
                holder.mTvSuperPrice.setText(text);
            }
            index++;
        }
    }

    @Override
    public int getItemCount() {
        return CommonUtil.isEmptyList(robotTrainItems) ? 0 : robotTrainItems.size();
    }

    class TrainItemViewHodler extends RecyclerView.ViewHolder {

        private TextView mTvStartTime;
        private TextView mTvEndTime;
        private TextView mTvStartStation;
        private TextView mTvEndStation;
        private TextView mTvAllTime;
        private TextView mTvTrainNo;
        private TextView mTvLowerPrice;
        private TextView mTvSecondaryPrice;
        private TextView mTvSuperPrice;

        private View mLine;

        TrainItemViewHodler(View itemView) {
            super(itemView);
            mTvStartTime = itemView.findViewById(R.id.start_time);
            mTvEndTime = itemView.findViewById(R.id.end_time);
            mTvStartStation = itemView.findViewById(R.id.start_station);
            mTvEndStation = itemView.findViewById(R.id.end_station);
            mTvAllTime = itemView.findViewById(R.id.all_time);
            mTvTrainNo = itemView.findViewById(R.id.train_no);
            mTvLowerPrice = itemView.findViewById(R.id.lower_prices);
            mTvSecondaryPrice = itemView.findViewById(R.id.secondary_prices);
            mTvSuperPrice = itemView.findViewById(R.id.super_prices);
            mLine = itemView.findViewById(R.id.robot_item_line);
        }
    }

}
