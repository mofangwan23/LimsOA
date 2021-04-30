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
import cn.flyrise.feep.location.adapter.SignInLeaderDayStatisDetailAdapter.ViewHolder;
import cn.flyrise.feep.location.bean.SignInLeaderDayDetail;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-19-13:57.
 */

public class SignInLeaderDayStatisDetailAdapter extends RecyclerView.Adapter<ViewHolder> {

	private List<SignInLeaderDayDetail> items;
	private Context mContext;

	public SignInLeaderDayStatisDetailAdapter(Context context) {
		this.mContext = context;
	}

	public void addItem(List<SignInLeaderDayDetail> items) {
		this.items = items;
		notifyDataSetChanged();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.location_leader_day_statis_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		SignInLeaderDayDetail item = items.get(position);
		if (item == null || TextUtils.isEmpty(item.userId)) return;
		if (item.sumTitle != null && item.sumTitle.length > 0) {
			StringBuilder sb = new StringBuilder();
			for (String text : item.sumTitle) {
				if (TextUtils.isEmpty(text)) continue;
				sb.append(text);
				sb.append("，");
			}
			holder.mTvState.setText(TextUtils.isEmpty(sb) ? "" : sb.substring(0, sb.length() - 1));
		}
		else {
			holder.mTvState.setText("");
		}
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
	}

	@Override
	public int getItemCount() {
		return CommonUtil.isEmptyList(items) ? 0 : items.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		private TextView mTvUserName;
		private TextView mTvUserDepart;
		private TextView mTvState;
		private ImageView mImgUserIcon;

		public ViewHolder(View itemView) {
			super(itemView);
			mImgUserIcon = itemView.findViewById(R.id.user_icon);
			mTvUserName = itemView.findViewById(R.id.user_name);
			mTvUserDepart = itemView.findViewById(R.id.user_department);
			mTvState = itemView.findViewById(R.id.item_state);
		}
	}
}
