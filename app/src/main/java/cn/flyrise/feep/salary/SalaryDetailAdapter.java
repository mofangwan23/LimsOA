package cn.flyrise.feep.salary;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.salary.SalaryDetailAdapter.ViewHolder;
import cn.flyrise.feep.salary.model.SalaryItem;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-02-16 10:33
 */
public class SalaryDetailAdapter extends RecyclerView.Adapter<ViewHolder> {

	private List<SalaryItem> mSalaryItems;

	public void setSalaryItems(List<SalaryItem> salaryItems) {
		this.mSalaryItems = salaryItems;
		this.notifyDataSetChanged();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(View.inflate(parent.getContext(), R.layout.item_salary_detail, null));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		SalaryItem salaryItem = mSalaryItems.get(position);
		holder.tvKey.setText(salaryItem.key);
		if (salaryItem.type == SalaryItem.TYPE_OTHER) {
			holder.tvValue.setText(salaryItem.value);
		}
		else {
			holder.tvValue.setText(BaseSalaryActivity.formatMonery(salaryItem.value));
		}
		SalaryItem nextSalaryItem = position + 1 == mSalaryItems.size() ? null : mSalaryItems.get(position + 1);
		if (nextSalaryItem == null) {
			holder.line.setVisibility(View.GONE);
			holder.splitLine.setVisibility(View.GONE);
		}
		else {
			boolean hasSameType = salaryItem.type == nextSalaryItem.type;
			holder.line.setVisibility(hasSameType ? View.VISIBLE : View.GONE);
			holder.splitLine.setVisibility(hasSameType ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override public int getItemCount() {
		return CommonUtil.isEmptyList(mSalaryItems) ? 0 : mSalaryItems.size();
	}

	 class ViewHolder extends RecyclerView.ViewHolder {

		private TextView tvKey;
		private TextView tvValue;
		private View line;
		private View splitLine;

		public ViewHolder(View view) {
			super(view);
			tvKey = view.findViewById(R.id.tvSalaryKey);
			tvValue = view.findViewById(R.id.tvSalaryValue);
			line = view.findViewById(R.id.viewSplitLine);
			splitLine = view.findViewById(R.id.viewSplitLine16);
		}
	}
}
