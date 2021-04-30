package cn.flyrise.feep.knowledge.adpater;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.knowledge.model.Folder;
import cn.flyrise.feep.media.common.FileCategoryTable;

/**
 * Created by klc
 */
public class MoveListAdapter extends KnowledgeListBaseAdapter<Folder> {

    private Context mContext;

    public MoveListAdapter(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Folder item = getItem(position);
        final ItemViewHolder viewHolder = (ItemViewHolder) holder;
        boolean isCanChoice = isCanChoice();
        if (isCanChoice)
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        else
            viewHolder.checkBox.setVisibility(View.GONE);
        if (item.isChoice)
            viewHolder.checkBox.setChecked(true);
        else
            viewHolder.checkBox.setChecked(false);
        FEImageLoader.load(mContext, viewHolder.imageView, FileCategoryTable.getIcon("dir"));
        viewHolder.fileName.setText(item.name);
        viewHolder.time.setVisibility(View.GONE);
        setListener(viewHolder, item);
    }
}