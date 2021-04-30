package cn.flyrise.feep.workplan7.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.workplan7.model.WorkPlanWaitSend;
import java.util.List;

/**
 * author : klc
 * data on 2018/5/7 17:01
 * Msg : 计划待发列表
 */
public class WorkPlanWaitSendAdapter extends FEListAdapter<WorkPlanWaitSend> {


	private boolean checkState;

	public boolean isCheckState() {
		return checkState;
	}

	public void setCheckState(boolean checkState) {
		this.checkState = checkState;
		if (!checkState) {
			for (WorkPlanWaitSend waitSend : dataList) {
				waitSend.isCheck = false;
			}
		}
		this.notifyDataSetChanged();
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ItemViewHolder viewHolder = (ItemViewHolder) holder;
		WorkPlanWaitSend waitingSend = dataList.get(position);
		viewHolder.tvTitle.setText(waitingSend.title);
		viewHolder.tvDate.setText(waitingSend.sendTime);
		viewHolder.checkBox.setVisibility(checkState ? View.VISIBLE : View.GONE);
		viewHolder.checkBox.setChecked(waitingSend.isCheck);
		viewHolder.itemView.setOnClickListener(v -> {
			if (checkState) {
				waitingSend.isCheck = !waitingSend.isCheck;
				viewHolder.checkBox.setChecked(waitingSend.isCheck);
				return;
			}
			if (onItemClickListener != null) {
				onItemClickListener.onItemClick(viewHolder.itemView, waitingSend);
			}
		});
		viewHolder.itemView.setOnLongClickListener(v -> {
			if (!checkState) {
				waitingSend.isCheck = true;
			}
			setCheckState(!checkState);
			if (onItemLongClickListener != null) {
				onItemLongClickListener.onItemLongClick(viewHolder.itemView, waitingSend);
			}
			return true;
		});
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View view = View.inflate(parent.getContext(), R.layout.workplan_waiting_send, null);
		return new ItemViewHolder(view);
	}


	private class ItemViewHolder extends RecyclerView.ViewHolder {

		private TextView tvTitle;
		private TextView tvDate;
		private CheckBox checkBox;

		public ItemViewHolder(View itemView) {
			super(itemView);
			tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
			tvDate = (TextView) itemView.findViewById(R.id.tvDate);
			checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
		}
	}

	public List<WorkPlanWaitSend> getDataList() {
		return this.dataList;
	}
}
