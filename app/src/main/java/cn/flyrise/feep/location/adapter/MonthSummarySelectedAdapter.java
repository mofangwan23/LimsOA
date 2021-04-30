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
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.bean.SignInLeaderMonthItem;
import java.util.List;

/**
 * 月汇总选择用户异常打卡类型
 */
public class MonthSummarySelectedAdapter extends BaseSelectedAdapter {

	private List<SignInLeaderMonthItem> lists;
	private final Context context;

	public MonthSummarySelectedAdapter(Context context, List<SignInLeaderMonthItem> personLists) {
		this.context = context;
		this.lists = personLists;
	}

	@Override
	public int getDataSourceCount() {
		return lists == null ? 0 : lists.size();
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ViewHolder viewHolder = (ViewHolder) holder;
		final SignInLeaderMonthItem person = lists.get(position);
		viewHolder.check.setVisibility(TextUtils.equals(id, String.valueOf(person.sumId)) ? View.VISIBLE : View.GONE);
		viewHolder.name.setText(person.sumTitle);
		viewHolder.layout.setOnClickListener(v -> {
			setCurrentPosition(String.valueOf(person.sumId));
			mListener.onSelectedClickeItem(String.valueOf(person.sumId), position);
		});
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.location_month_summary_selected_item, null));
	}

	private class ViewHolder extends RecyclerView.ViewHolder {

		public TextView name;
		public ImageView check;
		public View layout;

		public ViewHolder(View itemView) {
			super(itemView);
			check = itemView.findViewById(R.id.person_checked);
			name = itemView.findViewById(R.id.name);
			layout = itemView;
		}
	}

	public int getCurrentPosition() {
		if (CommonUtil.isEmptyList(lists)) {
			return 0;
		}
		for (int i = 0; i < lists.size(); i++) {
			if (lists.get(i) == null) {
				continue;
			}
			if (TextUtils.equals(id, String.valueOf(lists.get(i).sumId))) {
				return i;
			}
		}
		return 0;
	}
}
