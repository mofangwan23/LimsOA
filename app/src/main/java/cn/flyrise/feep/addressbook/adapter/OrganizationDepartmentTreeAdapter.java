package cn.flyrise.feep.addressbook.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.model.DepartmentNode;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2017-02-16 22:57
 */
public class OrganizationDepartmentTreeAdapter extends BaseAdapter {

    private String mDefaultNodeId;
    private List<DepartmentNode> mDepartmentNodes;

    public void setDefaultNodeId(String defaultNodeId) {
        this.mDefaultNodeId = defaultNodeId;
    }

    public String getDefaultNodeId() {
        return mDefaultNodeId;
    }

    public void setDepartmentNodes(List<DepartmentNode> departmentNodes) {
        this.mDepartmentNodes = departmentNodes;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return CommonUtil.isEmptyList(mDepartmentNodes) ? 0 : mDepartmentNodes.size();
    }

    @Override
    public Object getItem(int position) {
        return CommonUtil.isEmptyList(mDepartmentNodes) ? 0 : mDepartmentNodes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_organization_department, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DepartmentNode departmentNode = mDepartmentNodes.get(position);
        holder.name.setText(departmentNode.value.name);
        if (departmentNode.isLeafNode()) {
            holder.arrow.setVisibility(departmentNode.value != null
                    && TextUtils.equals(departmentNode.value.fatherId, "0")
                    ? View.GONE : View.INVISIBLE);
        } else {
            holder.arrow.setVisibility(View.VISIBLE);
            holder.arrow.setImageResource(departmentNode.isExpand
                    ? R.drawable.address_tree_department_ex
                    : R.drawable.address_tree_department_ec);
        }
        int level = departmentNode.value.level <= 1 ? 1 : departmentNode.value.level;
        convertView.setPadding(level * 30, 3, 3, 3);
        if (departmentNode.value.level == 0) {
            convertView.setBackgroundColor(parent.getResources().getColor(R.color.address_tree_department_list_item_normal));
        } else if (TextUtils.equals(mDefaultNodeId, departmentNode.value.deptId)) {
            convertView.setBackgroundColor(Color.WHITE);
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }

    private class ViewHolder {

        private TextView name;
        private ImageView arrow;

        public ViewHolder(View itemView) {
            name = (TextView) itemView.findViewById(R.id.tvDepartmentName);
            arrow = (ImageView) itemView.findViewById(R.id.ivDepartmentArrow);
        }
    }
}
