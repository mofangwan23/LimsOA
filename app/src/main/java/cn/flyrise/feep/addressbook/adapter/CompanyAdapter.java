package cn.flyrise.feep.addressbook.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.model.Department;

/**
 * @author ZYP
 * @since 2016-12-07 16:44
 */
public class CompanyAdapter extends OrgBaseAdapter<Department> {
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_address_book_position, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Department department = mOrgDatas.get(position);
        if (mDefault != null && TextUtils.equals(department.deptId, mDefault.deptId)) {
            holder.ivChecked.setVisibility(View.VISIBLE);
//            holder.tvPosition.setTextColor(parent.getContext().getResources().getColor(R.color.defaultColorAccent));
        }
        else {
            holder.ivChecked.setVisibility(View.INVISIBLE);
            holder.tvPosition.setTextColor(Color.parseColor("#17191A"));
        }

        holder.tvPosition.setText(department.name);
        return convertView;
    }
}