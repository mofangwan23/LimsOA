package cn.flyrise.feep.knowledge.adpater;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.knowledge.model.ListBaseItem;
import cn.flyrise.feep.knowledge.model.PubAndRecFile;
import cn.flyrise.feep.media.common.FileCategoryTable;

/**
 * Created by klc
 */


public class PublishedListAdapter extends KnowledgeListBaseAdapter<PubAndRecFile> {

	private Context mContext;
	private OnChoiceListener choiceListener;

	public PublishedListAdapter(Context mContext) {
		this.mContext = mContext;
	}

	public void setChoiceListener(OnChoiceListener choiceListener) {
		this.choiceListener = choiceListener;
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		final PubAndRecFile item = getItem(position);
		final ItemViewHolder viewHolder = (ItemViewHolder) holder;
		boolean canChoice = isCanChoice();
		if (canChoice)
			viewHolder.checkBox.setVisibility(View.VISIBLE);
		else
			viewHolder.checkBox.setVisibility(View.GONE);
		if (item.isChoice)
			viewHolder.checkBox.setChecked(true);
		else
			viewHolder.checkBox.setChecked(false);
		FEImageLoader.load(mContext, viewHolder.imageView, FileCategoryTable.getIcon(FileCategoryTable.getType(item.filetype)));
		String fileName = item.getRealFileName();
		viewHolder.fileName.setText(fileName);
		if ("不限".equals(item.enddate)) {
			viewHolder.time.setText(mContext.getString(R.string.date_available) + item.enddate);
		}
		else {
			viewHolder.time.setText(mContext.getString(R.string.date_available) + DateUtil.formatTimeForDetail(item.enddate));
		}
		viewHolder.llTime.setVisibility(View.VISIBLE);
		viewHolder.reciverTv.setText(mContext.getString(R.string.receiver) + item.roleid);
		viewHolder.reciverTv.setVisibility(View.VISIBLE);
		viewHolder.time.setVisibility(View.GONE);//罗伟豪说隐藏日期
		setListener(viewHolder, item);
	}

	@Override
	protected void onCheckBoxClickEvent(ListBaseItem file, KnowledgeListBaseAdapter.ItemViewHolder viewHolder) {
		super.onCheckBoxClickEvent(file, viewHolder);
		if (choiceListener != null)
			choiceListener.choiceStateListener(choiceCount);
	}


	@Override
	public void setCanChoice(boolean canCheck) {
		super.setCanChoice(canCheck);
		if (choiceListener != null)
			choiceListener.choiceStateListener(choiceCount);
	}

	protected void selectAll() {
		super.selectAll();
		if (choiceListener != null)
			choiceListener.choiceStateListener(choiceCount);
	}

	@Override
	protected void unSelectAll() {
		super.unSelectAll();
		if (choiceListener != null)
			choiceListener.choiceStateListener(choiceCount);
	}

	public interface OnChoiceListener {

		void choiceStateListener(int choiceCount);

	}
}
