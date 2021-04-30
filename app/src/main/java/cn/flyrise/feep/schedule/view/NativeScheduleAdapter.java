package cn.flyrise.feep.schedule.view;

import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.flyrise.android.protocol.entity.schedule.AgendaResponseItem;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2016-12-19 15:00
 */
public class NativeScheduleAdapter extends BaseAdapter {

	private List<AgendaResponseItem> mScheduleItems;
	private long mCurrentTime;

	public void setScheduleItems(List<AgendaResponseItem> items) {
		mCurrentTime = new Date().getTime();
		this.mScheduleItems = items;
		this.notifyDataSetChanged();
	}

	@Override public int getCount() {
		return CommonUtil.isEmptyList(mScheduleItems) ? 0 : mScheduleItems.size();
	}

	@Override public Object getItem(int position) {
		return CommonUtil.isEmptyList(mScheduleItems) ? null : mScheduleItems.get(position);
	}

	@Override public long getItemId(int position) {
		return position;
	}

	@Override public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(parent.getContext(), R.layout.item_native_schedule, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		AgendaResponseItem scheduleItem = mScheduleItems.get(position);
		holder.tvTitle.setText(scheduleItem.title);
		holder.tvTime.setText(scheduleItem.startTime.substring(11, 16));

		if (scheduleItem.isSharedEvent()) {
			holder.scheduleTypeView.setBackgroundResource(R.color.green_complete);
			CoreZygote.getAddressBookServices().queryUserDetail(scheduleItem.shareUserId)
					.subscribe(addressBook -> {
						holder.tvAuthor.setText("发送人：" + addressBook.name);
					}, error -> {

					});
		}
		else if (dateToBeOverdue(scheduleItem.endTime)) {
			holder.scheduleTypeView.setBackgroundResource(R.color.gray_pressed);
			holder.tvAuthor.setText("");
		}
		else {
			holder.scheduleTypeView.setBackgroundResource(R.color.defaultColorAccent);
			holder.tvAuthor.setText("");
		}
		return convertView;
	}

	private boolean dateToBeOverdue(String text) {//日程过期
		try {
			Calendar calendar = DateUtil.str2Calendar(text);
			return calendar != null && calendar.getTime() != null && mCurrentTime > calendar.getTime().getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String removeSchedule(String scheduleId) {
		AgendaResponseItem targetItem = null;
		for (AgendaResponseItem item : mScheduleItems) {
			if (TextUtils.equals(item.id, scheduleId)) {
				targetItem = item;
				break;
			}
		}

		if (targetItem != null) {
			mScheduleItems.remove(targetItem);
		}
		this.notifyDataSetChanged();
		return targetItem.eventSourceId;
	}

	public CharSequence stripHtml(String s) {
		return Html.fromHtml(s).toString()
				.replace('\n', (char) 32)
				.replace((char) 160, (char) 32)
				.replace((char) 65532, (char) 32)
				.trim();
	}

	private static class ViewHolder {

		TextView tvTitle;
		TextView tvAuthor;
		TextView tvTime;
		View scheduleTypeView;

		public ViewHolder(View convertView) {
			tvTitle = (TextView) convertView.findViewById(R.id.tvScheduleTitle);
			tvAuthor = (TextView) convertView.findViewById(R.id.tvScheduleAuthor);
			tvTime = (TextView) convertView.findViewById(R.id.tvScheduleTime);
			scheduleTypeView = convertView.findViewById(R.id.scheduleTypeView);
		}
	}
}
