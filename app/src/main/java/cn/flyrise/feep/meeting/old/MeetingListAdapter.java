package cn.flyrise.feep.meeting.old;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.utils.Patches;

public class MeetingListAdapter extends FEListAdapter<MeetingListItemBean> {

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		final ItemViewHolder viewHolder = (ItemViewHolder) holder;
		MeetingListItemBean itemBean = dataList.get(position);
		viewHolder.view.setBackgroundResource(R.drawable.listview_item_bg);
		viewHolder.tv_title.setText(itemBean.getTitle());
		viewHolder.tv_name.setText(itemBean.getSendUser());
		viewHolder.tv_time.setText(DateUtil.formatTimeForList(itemBean.getTime()));
		viewHolder.iv_imageView.setVisibility(View.GONE);
		viewHolder.tv_attend.setVisibility(View.VISIBLE);
		final String status = itemBean.getStatus();
		switch (status) {
			case "1": //参加
				viewHolder.iv_readState.setImageResource(R.drawable.meeting_attend);
				break;
			case "2": //不参加
				viewHolder.iv_readState.setImageResource(R.drawable.meeting_not_attend);
				break;
			case "3": //待定
				viewHolder.iv_readState.setImageResource(R.drawable.meeting_unknown);
				break;
			case "0": //未处理
				viewHolder.iv_readState.setImageResource(R.drawable.meeting_untreated_show);
				break;
			case "-2": //取消
				viewHolder.iv_readState.setImageResource(R.drawable.meeting_cancel_show);
				break;
			default:
				break;
		}
		final String endTime = itemBean.getEndTime();
		String meetingTimeout = itemBean.getStartTime();
		if (!TextUtils.isEmpty(endTime)) {
			meetingTimeout = FunctionManager.hasPatch(Patches.PATCH_MEETING_MANAGER) ? endTime : meetingTimeout;
		}
		if (DateUtil.isTimeout(meetingTimeout)) {
			viewHolder.iv_readState.setImageResource(R.drawable.meeting_timeout);
		}
		viewHolder.tv_attend.setVisibility(View.GONE);
		viewHolder.view.setOnClickListener(v -> {
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(viewHolder.view, itemBean);
		});
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.meeting_list_item, parent, false);
		return new ItemViewHolder(convertView);
	}

	private class ItemViewHolder extends RecyclerView.ViewHolder {

		View view;
		TextView tv_title;
		TextView tv_name;
		TextView tv_time;
		TextView tv_attend;
		ImageView iv_imageView;
		View meeting_line;
		ImageView iv_readState;

		ItemViewHolder(View itemView) {
			super(itemView);
			view = itemView;
			tv_title = (TextView) itemView.findViewById(R.id.fe_list_item_title);
			tv_name = (TextView) itemView.findViewById(R.id.fe_list_item_name);
			tv_time = (TextView) itemView.findViewById(R.id.fe_list_item_time);
			tv_attend = (TextView) itemView.findViewById(R.id.attend);
			iv_imageView = (ImageView) itemView.findViewById(R.id.fe_list_item_icon_arrow);
			meeting_line = itemView.findViewById(R.id.meeting_line);
			iv_readState = (ImageView) itemView.findViewById(R.id.read_state);
		}
	}
}
