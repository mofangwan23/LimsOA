package cn.flyrise.feep.location.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.location.Sign;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-13:37.
 * 月统计详情日期
 */

public class SignInMonthStatisSubItemAdapter extends RecyclerView.Adapter<SignInMonthStatisSubItemAdapter.ViewHodler> {

	private List<String> items;

	public List<Integer> days = new ArrayList<>();//不可点击制灰
	private OnClickeSubItemListener mListener;
	private Context mContext;
	private String[] weeks;
	private int mSumId;

	SignInMonthStatisSubItemAdapter(Context context, int sumId, OnClickeSubItemListener mListener) {
		mContext = context;
		weeks = mContext.getResources().getStringArray(R.array.schedule_weeks);
		this.mListener = mListener;
		this.mSumId = sumId;
		days.add(Sign.state.REST);
		days.add(Sign.state.ABSENTEEISM);
	}

	public void setData(String[] items) {
		this.items = Arrays.asList(items);
		notifyDataSetChanged();
	}

	@Override
	public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHodler(LayoutInflater.from(parent.getContext()).inflate(R.layout.location_month_summary_sub_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHodler holder, int position) {
		Calendar calendar = DateUtil.str2Calendar(items.get(position));
		if (calendar == null) return;
		holder.mTvTitle.setText(DateUtil.subDateYYYYMMDD(mContext, calendar) + "  " + weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
//		holder.mTvTitle.setTextColor(Color.parseColor(days.contains(mSumId) ? "#CDCDCD" : "#191919"));
		holder.mTvTitle.setOnClickListener(v -> {
			if (mListener != null) mListener.onClickListner(items.get(position), 0);
		});
	}

	@Override
	public int getItemCount() {
		return CommonUtil.isEmptyList(items) ? 0 : items.size();
	}

	class ViewHodler extends RecyclerView.ViewHolder {

		private TextView mTvTitle;

		ViewHodler(View itemView) {
			super(itemView);
			mTvTitle = itemView.findViewById(R.id.title);
		}
	}

	public interface OnClickeSubItemListener {

		void onClickListner(String date, int sumId);
	}

}
