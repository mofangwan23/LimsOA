package cn.flyrise.feep.collaboration.matter.adpater;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.matter.model.DirectoryNode;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;

/**
 * Created by klc on 2017/5/16.
 */

public class DirectoryAdapter extends BaseRecyclerAdapter {

    public List<DirectoryNode> nodeList;


    @Override
    public int getDataSourceCount() {
        return nodeList == null ? 0 : nodeList.size();
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DirectoryNodeHolder viewHolder = (DirectoryNodeHolder) holder;
        DirectoryNode node = nodeList.get(position);
        viewHolder.name.setText(node.name);
        viewHolder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(viewHolder.itemView, node);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matter_directory, parent, false);
        return new DirectoryNodeHolder(convertView);
    }


    public class DirectoryNodeHolder extends RecyclerView.ViewHolder {

        public TextView name;

        DirectoryNodeHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tvDepartmentName);
        }
    }


    public void setNodeList(List<DirectoryNode> nodeList) {
        this.nodeList = nodeList;
        this.notifyDataSetChanged();
    }

}
