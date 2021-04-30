package cn.flyrise.feep.addressbook.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.model.Department;

/**
 * @author ZYP
 * @since 2016-12-07 17:08
 */
public class SubDepartmentAdapter extends OrgBaseAdapter<Department> {

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_address_book_filter, null);
        }
        Department department = mOrgDatas.get(position);
        ((TextView) convertView).setText(department.name);

        if (mDefault != null && TextUtils.equals(mDefault.deptId, department.deptId)) {
            ((TextView) convertView).setTextColor(parent.getContext().getResources().getColor(R.color.defaultColorAccent));
        }
        else {
            ((TextView) convertView).setTextColor(parent.getContext().getResources().getColor(R.color.text_bright_color));
        }
        return convertView;
    }
}
