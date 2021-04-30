package cn.flyrise.feep.salary;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.flyrise.feep.core.common.utils.LanguageManager;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.salary.model.Salary;

/**
 * @author ZYP
 * @since 2017-04-18 11:56
 */
public class SalaryExpandableAdapter extends BaseExpandableListAdapter {

	private Map<String, List<Salary>> mYearSalaryMap;
	private List<String> mYearLists;
	private final DecimalFormat mMonthFormat = new DecimalFormat("00");
	private boolean isChinese;

	SalaryExpandableAdapter() {
		isChinese = LanguageManager.isChinese();
	}

	public void setYearSalaryMap(Map<String, List<Salary>> yearSalaryMap) {
		this.mYearSalaryMap = yearSalaryMap;
		this.mYearLists = this.mYearSalaryMap == null ? null : new ArrayList<>(mYearSalaryMap.keySet());
		this.notifyDataSetChanged();
	}

	@Override
	public int getGroupCount() {
		return CommonUtil.isEmptyList(mYearLists) ? 0 : mYearLists.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		String key = CommonUtil.isEmptyList(mYearLists) ? null : mYearLists.get(groupPosition);
		if (key == null) {
			return 0;
		}
		List<Salary> salaries = mYearSalaryMap == null ? null : mYearSalaryMap.get(key);
		return CommonUtil.isEmptyList(salaries) ? 0 : salaries.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return CommonUtil.isEmptyList(mYearLists) ? null : mYearLists.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		String key = CommonUtil.isEmptyList(mYearLists) ? null : mYearLists.get(groupPosition);
		if (key == null) {
			return null;
		}
		List<Salary> salaries = mYearSalaryMap == null ? null : mYearSalaryMap.get(key);
		return CommonUtil.isEmptyList(salaries) ? null : salaries.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return groupPosition + childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		GroupViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(parent.getContext(), R.layout.item_salary_expand_group, null);
			holder = new GroupViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (GroupViewHolder) convertView.getTag();
		}

		holder.tvSalaryYear.setText(mYearLists.get(groupPosition));
		holder.ivGroupIndicator.setImageResource(isExpanded
				? R.drawable.address_tree_department_ex
				: R.drawable.address_tree_department_ec);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ChildViewHolder holder = null;
		if (convertView == null) {
			convertView = View.inflate(parent.getContext(), R.layout.item_salary_expand_child, null);
			holder = new ChildViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ChildViewHolder) convertView.getTag();
		}

		String key = mYearLists.get(groupPosition);
		List<Salary> salaries = mYearSalaryMap.get(key);
		Salary salary = salaries.get(childPosition);

		if (isChinese) {
			holder.tvSalaryMonth
					.setText(mMonthFormat.format(CommonUtil.parseInt(salary.month)) + CommonUtil.getString(R.string.salary_lbl_month));
		}
		else {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MONTH, CommonUtil.parseInt(salary.month) - 1);
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
			String month = dateFormat.format(calendar.getTime());
			holder.tvSalaryMonth.setText(month);
		}

		holder.tvSalaryValue.setText(BaseSalaryActivity.formatMonery(salary.salary));

		if (childPosition == salaries.size() - 1) {
			holder.splitView.setVisibility(View.GONE);
		}
		else {
			holder.splitView.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private class ChildViewHolder {

		TextView tvSalaryMonth;
		TextView tvSalaryValue;
		View splitView;

		public ChildViewHolder(View itemView) {
			tvSalaryMonth = (TextView) itemView.findViewById(R.id.tvSalaryMonth);
			tvSalaryValue = (TextView) itemView.findViewById(R.id.tvSalaryValue);
			splitView = itemView.findViewById(R.id.viewSplitLine);
		}
	}

	private class GroupViewHolder {

		TextView tvSalaryYear;
		ImageView ivGroupIndicator;

		public GroupViewHolder(View itemView) {
			tvSalaryYear = (TextView) itemView.findViewById(R.id.tvSalaryYear);
			ivGroupIndicator = (ImageView) itemView.findViewById(R.id.ivGroupIndicator);
		}
	}

}
