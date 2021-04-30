package cn.flyrise.feep.location.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.location.Sign;
import cn.flyrise.feep.location.bean.SignInLeaderMonthDetail;
import cn.flyrise.feep.location.util.LocationBitmapUtil;
import java.util.Arrays;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-13:37.
 * 月统计详情
 */

public class SignInLeaderMonthStatisDetailAdapter extends RecyclerView.Adapter<SignInLeaderMonthStatisDetailAdapter.ViewHodler> {


	private int currentIndex = -1;
	private int type;
	private Context mContext;
	private OnClickeItemListener mListener;
	private List<Integer> days;//单位是天的
	//	private List<Integer> redItems;//红字
	private List<SignInLeaderMonthDetail> items;

	public SignInLeaderMonthStatisDetailAdapter(Context context, int type, OnClickeItemListener listener) {
		this.mContext = context;
		this.mListener = listener;
		this.type = type;
		days = Arrays.asList(Sign.state.suffixDays);
//		redItems = Arrays.asList(Sign.state.redFonts);
	}

	public void setData(List<SignInLeaderMonthDetail> items) {
		this.items = items;
		notifyDataSetChanged();
	}

	@Override
	public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHodler(
				LayoutInflater.from(parent.getContext()).inflate(R.layout.location_leader_month_summary_detail_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHodler holder, int position) {
		SignInLeaderMonthDetail item = items.get(position);
		if (item == null || TextUtils.isEmpty(item.userId)) return;
		CoreZygote.getAddressBookServices().queryUserDetail(item.userId)
				.subscribe(it -> {
					if (it != null) {
						holder.mTvUserName.setText(it.name);
						holder.mTvUserDepart.setText(it.deptName);
						FEImageLoader.load(mContext, holder.mImgUserIcon
								, CoreZygote.getLoginUserServices().getServerAddress() + it.imageHref, it.userId, it.name);
					}
					else {
						holder.mImgUserIcon.setImageResource(R.drawable.administrator_icon);
					}
				}, error -> {
					holder.mImgUserIcon.setImageResource(R.drawable.administrator_icon);
				});

		setSummaryTextColor(holder, item);
		holder.mTvSum.setText(String.format(mContext.getResources().getString(days.contains(type)
						? R.string.location_month_summary_day
						: R.string.location_month_summary_second)
				, CommonUtil.isEmptyList(item.dateItems) ? "0" : item.dateItems.length + ""));

		if (!CommonUtil.isEmptyList(item.dateItems)) {
			SignInMonthStatisSubItemAdapter mAdapter = new SignInMonthStatisSubItemAdapter(mContext, 0, null);
			holder.mRecyclerView.setAdapter(mAdapter);
			mAdapter.setData(item.dateItems);
		}

		holder.mTvMore.setOnClickListener(v -> {
			if (mListener != null) mListener.onMonthMore(item.userId);
		});

		holder.mLayout.setOnClickListener(CommonUtil.isEmptyList(item.dateItems) ? null : v -> {
			showDetaile(position, item);
			if (mListener != null) mListener.onMonthSummaryItem(position);
		});
		holder.mMoreLayout.setVisibility(View.GONE);
		holder.mRecyclerView.setVisibility(View.GONE);
		setSummaryIconColor(holder, item, position);
	}

	private void setSummaryIconColor(ViewHodler holder, SignInLeaderMonthDetail item, int position) {
		if (CommonUtil.isEmptyList(item.dateItems)) {//灰色
			holder.mImgIcon.setImageBitmap(LocationBitmapUtil.tintBitmap(mContext
					, R.drawable.icon_address_filter_down, Color.parseColor("#CDCDCD")));
			return;
		}
		if (item.isSwitch && currentIndex == position) {//向上
			holder.mRecyclerView.setVisibility(View.VISIBLE);
			holder.mMoreLayout.setVisibility(View.VISIBLE);
			holder.mImgIcon.setImageBitmap(LocationBitmapUtil.rotateBitmap(mContext, R.drawable.icon_address_filter_down));
			return;
		}
		holder.mImgIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_address_filter_down));
	}

	private void setSummaryTextColor(ViewHodler holder, SignInLeaderMonthDetail item) {
		if (CommonUtil.isEmptyList(item.dateItems)) {
			holder.mTvSum.setTextColor(Color.parseColor("#CDCDCD"));
			return;
		}
		holder.mTvSum.setTextColor(Color.parseColor("#8B8C8C"));
	}

	private void resetItemSwitch(SignInLeaderMonthDetail currentItem) {
		if (CommonUtil.isEmptyList(items)) return;
		for (SignInLeaderMonthDetail item : items) {
			if (currentItem == item || !item.isSwitch) continue;
			item.isSwitch = false;
		}
	}

	public void showDetaile(int index, SignInLeaderMonthDetail item) {
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


		private TextView mTvUserName;
		private TextView mTvUserDepart;
		private TextView mTvSum;
		private TextView mTvMore;
		private RelativeLayout mMoreLayout;
		private ImageView mImgUserIcon;
		private ImageView mImgIcon;
		private View mLayout;
		private RecyclerView mRecyclerView;

		ViewHodler(View itemView) {
			super(itemView);
			mLayout = itemView;
			mImgUserIcon = itemView.findViewById(R.id.user_icon);
			mTvUserName = itemView.findViewById(R.id.user_name);
			mTvUserDepart = itemView.findViewById(R.id.user_department);
			mTvSum = itemView.findViewById(R.id.item_sum);
			mTvMore = itemView.findViewById(R.id.show_more);
			mMoreLayout = itemView.findViewById(R.id.more_layout);
			mImgIcon = itemView.findViewById(R.id.head_right_icon);
			mRecyclerView = itemView.findViewById(R.id.location_month_summary_sub_item);
			mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
			mRecyclerView.requestDisallowInterceptTouchEvent(false);
		}
	}

	public SignInLeaderMonthDetail getItem(int index) {
		return CommonUtil.isEmptyList(items) || index >= items.size() ? null : items.get(index);
	}

	public interface OnClickeItemListener {

		void onMonthSummaryItem(int position);

		void onMonthMore(String userId);
	}
}
