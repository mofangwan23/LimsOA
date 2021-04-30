package cn.flyrise.feep.location.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.location.bean.SignInFieldPersonnel;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 新建：陈冕;
 * 日期： 2018-5-19-13:57.
 * 外勤人员
 */

public class SignInLeaderDayStatisFieldPersonnelAdapter extends
		RecyclerView.Adapter<SignInLeaderDayStatisFieldPersonnelAdapter.ViewHolder> {

	private List<SignInFieldPersonnel> items;
	private Context mContext;
	private OnFieldPersonnelItemListener mListener;

	public SignInLeaderDayStatisFieldPersonnelAdapter(Context context, OnFieldPersonnelItemListener listener) {
		this.mContext = context;
		this.mListener = listener;
	}

	public void addItem(List<SignInFieldPersonnel> items) {
		this.items = items;
		notifyDataSetChanged();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(
				LayoutInflater.from(parent.getContext()).inflate(R.layout.location_leader_field_personnel_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		SignInFieldPersonnel item = items.get(position);
		if (item == null || TextUtils.isEmpty(item.userId)) return;
//		AddressBook addressBook = CoreZygote.getAddressBookServices().queryUserInfo(item.userId);
//		holder.mTvUserName.setText(addressBook.name);
//		holder.mTvUserDepart.setText(addressBook.deptName);
		holder.mTvTime.setText(item.time);
		holder.mTvAddress.setText(item.address);
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

		holder.itemView.setOnClickListener(v -> {
			if (mListener != null) mListener.onFieldPersonnelItem(item);
		});
	}

	@Override
	public int getItemCount() {
		return CommonUtil.isEmptyList(items) ? 0 : items.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		private TextView mTvUserName;
		private TextView mTvUserDepart;
		private TextView mTvTime;
		private TextView mTvAddress;
		private ImageView mImgUserIcon;
		private View itemView;

		public ViewHolder(View itemView) {
			super(itemView);
			this.itemView = itemView;
			mImgUserIcon = itemView.findViewById(R.id.user_icon);
			mTvUserName = itemView.findViewById(R.id.user_name);
			mTvUserDepart = itemView.findViewById(R.id.user_department);
			mTvTime = itemView.findViewById(R.id.user_time);
			mTvAddress = itemView.findViewById(R.id.user_address);
		}
	}

	public interface OnFieldPersonnelItemListener {

		void onFieldPersonnelItem(SignInFieldPersonnel item);
	}
}
