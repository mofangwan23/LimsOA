package cn.flyrise.feep.location.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.Sign;
import cn.flyrise.feep.location.adapter.SignInMonthStatisSubItemAdapter.OnClickeSubItemListener;
import cn.flyrise.feep.location.bean.SignInMonthStatisItem;
import cn.flyrise.feep.location.util.LocationBitmapUtil;
import java.util.Arrays;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-13:37.
 * 月汇总适配器
 */

public class SignInMonthStatisAdapter extends RecyclerView.Adapter<SignInMonthStatisAdapter.ViewHodler> {

	private List<SignInMonthStatisItem> items;
	private int currentIndex = 0;

	private List<Integer> days;//单位是天的
	private List<Integer> redItems;//红字
	private Context mContext;
	private OnClickeItemListener mListener;
	private OnClickeSubItemListener mSubListener;

	public SignInMonthStatisAdapter(Context context, OnClickeItemListener listener, OnClickeSubItemListener mListener) {
		this.mSubListener = mListener;
		this.mContext = context;
		this.mListener = listener;
		days = Arrays.asList(Sign.state.suffixDays);
		redItems = Arrays.asList(Sign.state.redFonts);
	}

	public void setData(List<SignInMonthStatisItem> statisItems) {
		this.items = statisItems;
		if (CommonUtil.nonEmptyList(items) && items.get(currentIndex) != null) {
			items.get(currentIndex).isSwitch = true;
		}
		notifyDataSetChanged();
	}

	@Override
	public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHodler(LayoutInflater.from(parent.getContext()).inflate(R.layout.location_month_summary_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHodler holder, int position) {
		SignInMonthStatisItem item = items.get(position);
		if (item == null) return;
		holder.mTvTitle.setText(item.sumTitle);
		setSummaryTextColor(holder, item);

		holder.mTvSum.setText(String.format(mContext.getResources().getString(days.contains(item.sumId)
						? R.string.location_month_summary_day
						: R.string.location_month_summary_second)
				, CommonUtil.isEmptyList(item.subItems) ? "0" : item.subItems.length + ""));

		if (!CommonUtil.isEmptyList(item.subItems)) {
			SignInMonthStatisSubItemAdapter mAdapter = new SignInMonthStatisSubItemAdapter(mContext,item.sumId, (text, sumId) -> {
				if (mSubListener != null) mSubListener.onClickListner(text, item.sumId);
			});
			holder.mRecyclerView.setAdapter(mAdapter);
			mAdapter.setData(item.subItems);
		}

		holder.mLayout.setOnClickListener(v -> {
			showDetaile(position);
			if (mListener != null) mListener.onMonthSummaryItem(position);
		});
		holder.mRecyclerView.setVisibility(View.GONE);
		setSummaryIconColor(holder, item, position);
	}

	private void setSummaryIconColor(ViewHodler holder, SignInMonthStatisItem item, int position) {
		if (CommonUtil.isEmptyList(item.subItems)) {//灰色
			holder.mImgIcon.setImageBitmap(LocationBitmapUtil.tintBitmap(mContext
					, R.drawable.icon_address_filter_down, Color.parseColor("#CDCDCD")));
			return;
		}
		if (item.isSwitch && currentIndex == position) {//向上
			holder.mRecyclerView.setVisibility(View.VISIBLE);
			holder.mImgIcon.setImageBitmap(LocationBitmapUtil.rotateBitmap(mContext, R.drawable.icon_address_filter_down));
			return;
		}
		holder.mImgIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_address_filter_down));
	}

	private void setSummaryTextColor(ViewHodler holder, SignInMonthStatisItem item) {
		if (CommonUtil.isEmptyList(item.subItems)) {
			holder.mTvSum.setTextColor(Color.parseColor("#CDCDCD"));
			return;
		}
		holder.mTvSum.setTextColor(Color.parseColor(redItems.contains(item.sumId) ? "#E60026" : "#191919"));
	}

	private void resetItemSwitch(SignInMonthStatisItem currentItem) {
		if (CommonUtil.isEmptyList(items)) return;
		for (SignInMonthStatisItem item : items) {
			if (currentItem == item || !item.isSwitch) continue;
			item.isSwitch = false;
		}
	}

	public void showDetaile(int index) {
		SignInMonthStatisItem item = getItem(index);
		if (index >= items.size() || item == null) return;
		currentIndex = index;
		item.isSwitch = !item.isSwitch;
		resetItemSwitch(item);
		notifyDataSetChanged();
	}


	@Override
	public int getItemCount() {
		return CommonUtil.isEmptyList(items) ? 0 : items.size();
	}

	class ViewHodler extends RecyclerView.ViewHolder {

		private TextView mTvTitle;
		private TextView mTvSum;
		private ImageView mImgIcon;
		private View mLayout;
		private RecyclerView mRecyclerView;

		ViewHodler(View itemView) {
			super(itemView);
			mLayout = itemView;
			mTvTitle = itemView.findViewById(R.id.item_title);
			mTvSum = itemView.findViewById(R.id.item_sum);
			mImgIcon = itemView.findViewById(R.id.head_right_icon);
			mRecyclerView = itemView.findViewById(R.id.location_month_summary_sub_item);
			mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
			mRecyclerView.requestDisallowInterceptTouchEvent(false);
		}
	}

	public SignInMonthStatisItem getItem(int index) {
		return index < 0 || CommonUtil.isEmptyList(items) || index >= items.size() ? null : items.get(index);
	}

	public interface OnClickeItemListener {

		void onMonthSummaryItem(int position);
	}
}
