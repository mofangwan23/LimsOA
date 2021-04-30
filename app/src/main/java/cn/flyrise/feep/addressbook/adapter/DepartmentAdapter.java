package cn.flyrise.feep.addressbook.adapter;

import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.model.Department;

/**
 * @author ZYP
 * @since 2016-12-07 17:07
 */
public class DepartmentAdapter extends OrgBaseAdapter<Department> {

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_address_book_filter, null);
        }

        Department department = mOrgDatas.get(position);
        if (mDefault != null && TextUtils.equals(department.deptId, mDefault.deptId)) {
            convertView.setBackgroundColor(parent.getResources().getColor(R.color.ab_pop_sub_dept_bg));
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                convertView.setBackground(parent.getResources().getDrawable(R.drawable.address_book_department_selector));
            }
            else {
                convertView.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.address_book_department_selector));
            }
        }
        ((TextView) convertView).setText(department.name);
        return convertView;
    }
}
