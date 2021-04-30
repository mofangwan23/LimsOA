package cn.flyrise.feep.knowledge.adpater;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.knowledge.model.PubAndRecFile;
import cn.flyrise.feep.media.common.FileCategoryTable;

/**
 * Created by klc
 */

public class ReceiverFileListAdapter extends KnowledgeListBaseAdapter<PubAndRecFile> {

    private Context mContext;

    public ReceiverFileListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final PubAndRecFile item = getItem(position);
        final ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.checkBox.setVisibility(View.GONE);
        FEImageLoader.load(mContext, viewHolder.imageView, FileCategoryTable.getIcon(FileCategoryTable.getType(item.filetype)));
        String fileName = item.getRealFileName();
        viewHolder.fileName.setText(fileName);
        viewHolder.time.setText(mContext.getString(R.string.publisher) + item.publishuser);
        viewHolder.time.setVisibility(View.VISIBLE);
        viewHolder.llTime.setVisibility(View.VISIBLE);
        //不排除将来会做成可以多选模式
        viewHolder.view.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(viewHolder.view, item);
        });
    }
}
