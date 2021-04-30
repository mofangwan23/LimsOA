package cn.flyrise.feep.salary;

import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2017-02-20 16:47
 */
public class SalaryListAdapter extends BaseAdapter {

    private List<Pair<String, String>> mMonths;
    private final DecimalFormat mMonthFormat = new DecimalFormat("00");

    public void setSalaryMonths(List<Pair<String, String>> months) {
        this.mMonths = months;
        this.notifyDataSetChanged();
    }

    @Override public int getCount() {
        return CommonUtil.isEmptyList(mMonths) ? 0 : mMonths.size();
    }

    @Override public Object getItem(int position) {
        return CommonUtil.isEmptyList(mMonths) ? null : mMonths.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_salary_list, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Pair<String, String> month = mMonths.get(position);
        String[] date = parseDate(month.first);     // date[0] = year  date[1] = month
        holder.tvMonth.setText(mMonthFormat.format(CommonUtil.parseInt(date[1])) + CommonUtil.getString(R.string.salary_lbl_month)); // 月份
        holder.tvSalary.setText(BaseSalaryActivity.formatMonery(month.second));
        holder.tvYear.setText(date[0] + CommonUtil.getString(R.string.salary_lbl_year));

        Pair<String, String> preMonth = position == 0 ? null : mMonths.get(position - 1);
        if (preMonth == null) {
            holder.tvYear.setVisibility(View.VISIBLE);
        }
        else {
            String[] preDate = parseDate(preMonth.first);
            boolean hasSameYear = TextUtils.equals(date[0], preDate[0]);
            holder.tvYear.setVisibility(hasSameYear ? View.GONE : View.VISIBLE);
        }

        Pair<String, String> nextMonth = position + 1 == mMonths.size() ? null : mMonths.get(position + 1);
        if (nextMonth == null) {
            holder.splitLine.setVisibility(View.GONE);
        }
        else {
            String[] nextDate = parseDate(nextMonth.first);
            boolean hasSameYear = TextUtils.equals(date[0], nextDate[0]);
            holder.splitLine.setVisibility(hasSameYear ? View.VISIBLE : View.GONE);
        }
        return convertView;
    }

    private String[] parseDate(String key) {
        return key.split("-");
    }

    private class ViewHolder {
        private TextView tvYear;
        private TextView tvMonth;
        private TextView tvSalary;
        private View splitLine;

        public ViewHolder(View itemView) {
            tvYear = (TextView) itemView.findViewById(R.id.tvSalaryYear);
            tvMonth = (TextView) itemView.findViewById(R.id.tvSalaryMonth);
            tvSalary = (TextView) itemView.findViewById(R.id.tvSalaryValue);
            splitLine = itemView.findViewById(R.id.viewSplitLine);
        }
    }


}