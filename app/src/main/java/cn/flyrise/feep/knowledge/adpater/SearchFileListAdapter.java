package cn.flyrise.feep.knowledge.adpater;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.knowledge.model.SearchFile;
import cn.flyrise.feep.media.common.FileCategoryTable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by klc
 */


public class SearchFileListAdapter extends BaseRecyclerAdapter {

    private List<SearchFile> files;
    private Context mContext;

    public SearchFileListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void onRefresh(List<SearchFile> files) {
        this.files = files;
        this.notifyDataSetChanged();
    }

    public void addData(List<SearchFile> dataList) {
        if (dataList == null) {
            this.files = new ArrayList<>();
        }
        this.files.addAll(dataList);
        this.notifyDataSetChanged();
    }

    @Override
    public int getDataSourceCount() {
        return files == null ? 0 : files.size();
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final SearchFile searchFile = files.get(position);
        final SearchFileViewHolder viewHolder = (SearchFileViewHolder) holder;
        FEImageLoader.load(mContext, viewHolder.fileTypeIv, FileCategoryTable.getIcon(FileCategoryTable.getType(searchFile.fileattr)));
        viewHolder.fileNameTv.setText(searchFile.remark.substring(searchFile.remark.lastIndexOf("/") + 1));
        viewHolder.directoriesTv.setText(searchFile.remark);
        viewHolder.view.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(viewHolder.view, searchFile);
        });
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.knowledge_list_item, parent, false);
        return new SearchFileViewHolder(convertView);
    }

    private class SearchFileViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView fileTypeIv;
        TextView fileNameTv;
        TextView directoriesTv;

        SearchFileViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.fileTypeIv = (ImageView) itemView.findViewById(R.id.file_icon);
            this.fileNameTv = (TextView) itemView.findViewById(R.id.file_name);
            this.directoriesTv = (TextView) itemView.findViewById(R.id.time);
        }
    }


}
