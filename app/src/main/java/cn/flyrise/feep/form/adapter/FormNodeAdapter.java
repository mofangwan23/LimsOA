//
// feep
//
// Created by ZhongYJ on 2012-02-10.
// Copyright 2011 flyrise. All rights reserved.
//

package cn.flyrise.feep.form.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.flyrise.android.protocol.model.FormNodeItem;
import cn.flyrise.android.protocol.model.FormNodeItem.FromNodeType;
import cn.flyrise.feep.R;
import cn.flyrise.feep.form.been.FormNodeToSubNode;

public class FormNodeAdapter extends BaseAdapter {
    private final Context mContext;

    private final ArrayList<FormNodeToSubNode> formNodeToSubNodes = new ArrayList<> ();

    private final ArrayList<String> nodeStrings = new ArrayList<> ();

    private final ArrayList<String> nodeNames = new ArrayList<> ();

    private int mSelectedPosition = 0;

    private boolean isDispose;

    class ViewHolder {
        public TextView nameTV;
        public View line;
    }

    public FormNodeAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        if (isDispose) {
            if (formNodeToSubNodes.size () == 0) {
                return 0;
            }
            return formNodeToSubNodes.size();
        } else {
            return nodeNames.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.form_node_listitem, null);
            holder.nameTV = (TextView) convertView.findViewById(R.id.form_node_list_item_name);
            holder.line = convertView.findViewById(R.id.form_node_line);
            // (TextView)convertView.findViewById(R.id.form_node_list_item_nodes);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mSelectedPosition == position) {
            convertView.setBackgroundResource(R.drawable.form_dispose_listview_item_checked);
        } else {
            convertView.setBackgroundResource(R.drawable.form_dispose_listview_item_selecter);
        }
        if (position == 0) {
            holder.line.setVisibility(View.VISIBLE);
        } else {
            holder.line.setVisibility(View.GONE);
        }
        String nodeName = null;
        String subnodeName = null;
        if (isDispose) {
            final FormNodeItem nodeItem = formNodeToSubNodes.get(position).getFormNodeItem();
            if (nodeItem != null) {
                if (nodeItem.getType() == FromNodeType.FromNodeTypeUnion || nodeItem.getType() == FromNodeType.FromNodeTypeOrion) {
                    holder.nameTV.setTextColor(0xFFFF0000);
                    subnodeName = "";
                } else {
                    holder.nameTV.setTextColor(0xFF000000);
                    subnodeName = nodeStrings.get(position);
                }
                nodeName = nodeItem.getName();
            }
        } else {
            nodeName = nodeNames.get(position);
            subnodeName = nodeStrings.get(position);
        }
        if ("".equals(subnodeName)) {
            holder.nameTV.setText(nodeName);
        } else {
            holder.nameTV.setText(nodeName + "=>" + subnodeName);
        }
        return convertView;
    }

    public void refreshDatas(ArrayList<FormNodeToSubNode> formNodeToSubNodes) {
        this.formNodeToSubNodes.clear();
        nodeStrings.clear();
        isDispose = true;
        if (formNodeToSubNodes != null) {
            for (final FormNodeToSubNode nodeToSubNode : formNodeToSubNodes) {
                final FormNodeItem nodeItem = nodeToSubNode.getFormNodeItem();
                if (nodeItem != null) {
                    final FromNodeType nodeType = nodeItem.getType();
                    if (nodeType != FromNodeType.FromNodeTypeLogic) {// 隐藏逻辑节点
                        this.formNodeToSubNodes.add(nodeToSubNode);
                        nodeStrings.add("");// 为节点集合填充数据
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 退回时，刷新节点列表
     *
     * @param nodeItem
     */
    public void refreshSendBackDatas(FormNodeItem nodeItem) {
        isDispose = false;
        nodeNames.clear();
        nodeStrings.clear();
        nodeNames.add(nodeItem.getName());
        nodeStrings.add("");
        notifyDataSetChanged();
    }

    /**
     * 设置显示的节点
     */
    public void addNodesString(String nodesString, int position) {
        if (position < nodeNames.size()) {
            nodeStrings.remove(position);
        }
        nodeStrings.add(position, nodesString);
        notifyDataSetChanged();
    }

}
