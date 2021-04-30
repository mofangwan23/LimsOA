package cn.flyrise.feep.knowledge.adpater;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.knowledge.model.FileDetail;
import cn.flyrise.feep.media.common.FileCategoryTable;

/**
 * Created by klc
 */

public class RecFileListFormMsgAdapter extends KnowledgeListBaseAdapter<FileDetail> {

    private Context mContext;

    public RecFileListFormMsgAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FileDetail item = getItem(position);
        final ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.checkBox.setVisibility(View.GONE);
        FEImageLoader.load(mContext, viewHolder.imageView, FileCategoryTable.getIcon(FileCategoryTable.getType(item.getFiletype())));
        String fileName = item.getTitle() + item.getFiletype();
        viewHolder.fileName.setText(fileName);
        viewHolder.time.setText(mContext.getString(R.string.publisher) + item.getPubUserName());
        viewHolder.time.setVisibility(View.VISIBLE);
        //不排除将来会做成可以多选模式
        viewHolder.view.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(viewHolder.view, item);
        });
    }
}
