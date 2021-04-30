package cn.flyrise.feep.location.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.Sign;
import cn.flyrise.feep.location.bean.SignInLeaderMonthItem;
import java.util.Arrays;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-13:37.
 */

public class SignInLeaderMonthStatisAdapter extends RecyclerView.Adapter<SignInLeaderMonthStatisAdapter.ViewHodler> {

	private List<SignInLeaderMonthItem> items;

	private List<Integer> redItems;//红字
	private Context mContext;
	private OnClickeItemListener mListener;

	public SignInLeaderMonthStatisAdapter(Context context, OnClickeItemListener listener) {
		this.mContext = context;
		this.mListener = listener;
		redItems = Arrays.asList(Sign.state.redFonts);
	}

	public void setData(List<SignInLeaderMonthItem> items) {
		this.items = items;
		notifyDataSetChanged();
	}

	@Override
	public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHodler(LayoutInflater.from(parent.getContext()).inflate(R.layout.location_leader_month_summary_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHodler holder, int position) {
		SignInLeaderMonthItem item = items.get(position);
		if (item == null) return;
		holder.mTvTitle.setText(item.sumTitle);
		holder.mImgIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.user_info_right_icon));
		holder.mImgIcon.setVisibility(item.userCount == 0 ? View.GONE : View.VISIBLE);
		if (item.userCount == 0) {
			holder.mTvSum.setTextColor(mContext.getResources().getColor(R.color.text_light_color));
		}
		else {
			holder.mTvSum.setTextColor(redItems.contains(item.sumId) ? Color.parseColor("#FF3B2F")
					: Color.parseColor("#191919"));
		}

		holder.mTvSum.setText(String.format(mContext.getResources().getString(R.string.location_leader_month_summary_second)
				, item.userCount, item.count));
		holder.mLayout.setOnClickListener(item.userCount == 0 ? null : v -> {
			if (mListener != null) mListener.onLearderMonthSummaryItem(position);
		});
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

		ViewHodler(View itemView) {
			super(itemView);
			mLayout = itemView;
			mTvTitle = itemView.findViewById(R.id.item_title);
			mTvSum = itemView.findViewById(R.id.item_sum);
			mImgIcon = itemView.findViewById(R.id.head_right_icon);
		}
	}

	public SignInLeaderMonthItem getItem(int index) {
		return CommonUtil.isEmptyList(items) || index >= items.size() ? null : items.get(index);
	}

	public interface OnClickeItemListener {

		void onLearderMonthSummaryItem(int position);
	}
}
