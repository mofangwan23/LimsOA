package cn.flyrise.feep.robot.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.entity.RobotHolidayItem;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-1-4-17:36.
 */

public class HolidaySubAdapter extends RecyclerView.Adapter<HolidaySubAdapter.HolidaySubViewHodler> {

	private List<RobotHolidayItem> robotHolidayItems;

	public HolidaySubAdapter(List<RobotHolidayItem> robotHolidayItems) {
		this.robotHolidayItems = robotHolidayItems;
	}

	@Override
	public HolidaySubViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
		return new HolidaySubViewHodler(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.robot_holiday_item_layout, parent, false));
	}

	@Override
	public void onBindViewHolder(HolidaySubViewHodler holder, int position) {
		if (robotHolidayItems == null) {
			return;
		}
		RobotHolidayItem holidayItem = robotHolidayItems.get(position);
		if (holidayItem == null) {
			return;
		}
		holder.mTvName.setText(holidayItem.name);
		holder.mTvTime.setText(holidayItem.startDate + "至" + holidayItem.endDate);
		holder.mTvDuration.setText(holidayItem.duration);
		holder.mTvWorkDay.setText(holidayItem.workDay);
		if (position == robotHolidayItems.size() - 1) {
			holder.mLine.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		return CommonUtil.isEmptyList(robotHolidayItems) ? 0 : robotHolidayItems.size();
	}

	class HolidaySubViewHodler extends RecyclerView.ViewHolder {

		private TextView mTvName;
		private TextView mTvTime;
		private TextView mTvDuration;
		private TextView mTvWorkDay;

		private View mLine;

		HolidaySubViewHodler(View itemView) {
			super(itemView);
			mTvName = itemView.findViewById(R.id.holiday_name);
			mTvTime = itemView.findViewById(R.id.holiday_time_content);
			mTvDuration = itemView.findViewById(R.id.holiday_duration);
			mTvWorkDay = itemView.findViewById(R.id.holiday_work);
			mLine = itemView.findViewById(R.id.robot_item_line);
		}
	}
}
