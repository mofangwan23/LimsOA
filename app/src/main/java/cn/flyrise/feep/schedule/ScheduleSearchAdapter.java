package cn.flyrise.feep.schedule;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.android.protocol.entity.schedule.AgendaResponseItem;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.core.services.model.AddressBook;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-11 10:58
 */
public class ScheduleSearchAdapter extends FEListAdapter<AgendaResponseItem> {

	@Override public void onChildBindViewHolder(ViewHolder holder, int position) {
		AgendaResponseItem scheduleItem = dataList.get(position);
		ItemViewHolder vHolder = (ItemViewHolder) holder;

		vHolder.tvScheduleTitle.setText(scheduleItem.title);
		vHolder.tvScheduleContent.setText(TextUtils.isEmpty(scheduleItem.content) ? "" : Html.fromHtml(scheduleItem.content));
		vHolder.tvScheduleSendTime.setText(scheduleItem.startTime);

		CoreZygote.getAddressBookServices().queryUserDetail(scheduleItem.shareUserId)
				.subscribe(addressBook -> {
					if (addressBook != null) {
						vHolder.tvScheduleAuthor.setVisibility(View.VISIBLE);
						vHolder.tvScheduleAuthor.setText(addressBook.name);
					}
					else {
						vHolder.tvScheduleAuthor.setVisibility(View.GONE);
					}
				}, error -> {
					vHolder.tvScheduleAuthor.setVisibility(View.GONE);
				});

//		AddressBook addressBook = CoreZygote.getAddressBookServices().queryUserInfo(scheduleItem.shareUserId);


		vHolder.itemView.setOnClickListener(v -> {
			if (onItemClickListener != null)
				onItemClickListener.onItemClick(vHolder.itemView, scheduleItem);
		});

	}

	@Override public ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_search, parent, false);
		return new ItemViewHolder(convertView);
	}

	private class ItemViewHolder extends RecyclerView.ViewHolder {

		public TextView tvScheduleTitle;
		public TextView tvScheduleContent;
		public TextView tvScheduleAuthor;
		public TextView tvScheduleSendTime;

		public ItemViewHolder(View itemView) {
			super(itemView);
			tvScheduleTitle = itemView.findViewById(R.id.tvScheduleTitle);
			tvScheduleContent = itemView.findViewById(R.id.tvScheduleContent);
			tvScheduleAuthor = itemView.findViewById(R.id.tvScheduleAuthor);
			tvScheduleSendTime = itemView.findViewById(R.id.tvScheduleSendTime);
		}
	}

}
