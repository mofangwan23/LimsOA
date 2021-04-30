package cn.flyrise.feep.knowledge.adpater;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.knowledge.model.FileAndFolder;
import cn.flyrise.feep.knowledge.model.ListBaseItem;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import cn.flyrise.feep.media.common.FileCategoryTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KLC on 2016/12/6.
 */
public class FolderFileListAdapter extends KnowledgeListBaseAdapter<FileAndFolder> {

	private Context mContext;
	private List<FileAndFolder> clickFolderList;
	private List<FileAndFolder> clickFileList;
	private OnChoiceListener choiceListener;
	private onItemClickListener clickListener;
	public boolean isEditStand;//处于编辑状态
	public boolean isFolderFragment;


	public FolderFileListAdapter(Context context, boolean isFolderFragment) {
		super();
		this.mContext = context;
		this.isFolderFragment = isFolderFragment;
		clickFolderList = new ArrayList<>();
		clickFileList = new ArrayList<>();
	}

	public List<FileAndFolder> getSelectedFiles() {
		return clickFileList;
	}

	public List<FileAndFolder> getClickFolderList() {
		return clickFolderList;
	}

	public void setChoiceListener(OnChoiceListener choiceListener) {
		this.choiceListener = choiceListener;
	}

	public void setOnItemClickListener(onItemClickListener clickListener) {
		this.clickListener = clickListener;
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		final FileAndFolder item = getItem(position);
		final ItemViewHolder viewHolder = (ItemViewHolder) holder;
		if (isEditStand) {
			for (FileAndFolder fileAndFolder : dataList) {
				((ListBaseItem) fileAndFolder).isClick = true;
			}
		}
		if (isFolderFragment && ("个人文档".equals(item.foldername) || "个人图片".equals(item.foldername))) {
			viewHolder.checkBox.setVisibility(View.GONE);
		}
		else {
			viewHolder.checkBox.setVisibility(View.VISIBLE);
		}
		if (item.isClick) {
			viewHolder.checkBox.setButtonDrawable(mContext.getResources().getDrawable(R.drawable.checkbox_blue_selected));
		}
		else {
			viewHolder.checkBox.setButtonDrawable(mContext.getResources().getDrawable(R.drawable.shape_circle_grey_10));
			viewHolder.checkBox.setChecked(false);
		}
		if (item.isChoice) {
			viewHolder.checkBox.setChecked(true);
		}
		else {
			viewHolder.checkBox.setChecked(false);
		}
		if (item.isFolder()) {
			FEImageLoader.load(mContext, viewHolder.imageView, FileCategoryTable.getIcon("dir"));
			viewHolder.fileName.setText(item.foldername);
		}
		else {
			FEImageLoader.load(mContext, viewHolder.imageView, FileCategoryTable.getIcon(FileCategoryTable.getType(item.filetype)));
			viewHolder.fileName.setText(item.getFileRealName());
		}
		String pubTime = item.pubTime;
		if (TextUtils.isEmpty(pubTime))
			viewHolder.llTime.setVisibility(View.GONE);
		else {
			viewHolder.llTime.setVisibility(View.VISIBLE);
			if (item.isFolder()) {
				viewHolder.time.setText(DateUtil.formatTimeForList(pubTime));
			}
			else {
				viewHolder.time.setText(DateUtil.formatTimeForList(pubTime) + "  |  " + item.filesize);
			}
		}

		setItemClickListener(viewHolder, item);

		setOnCheckBoxClickListener(viewHolder, item);

	}

	private void setItemClickListener(ItemViewHolder viewHolder, FileAndFolder item) {

		viewHolder.view.setOnClickListener(v -> {
			if (isFolderFragment && isEditStand && !"个人文档".equals(item.foldername) && !"个人图片".equals(item.foldername)) {
				onCheckBoxClickEvent(item, viewHolder);
			}
			else {
				clickListener.onClickListener(item);
			}
		});

		viewHolder.view.setOnLongClickListener(v -> {
			if (isFolderFragment && ("个人文档".equals(item.foldername) || "个人图片".equals(item.foldername))) {
				return false;
			}
			onCheckBoxClickEvent(item, viewHolder);
			if (onItemLongClickListener != null) {
				onItemLongClickListener.onItemLongClick(viewHolder.view, item);
				return true;
			}
			return false;
		});
	}

	private void setOnCheckBoxClickListener(ItemViewHolder viewHolder, FileAndFolder item) {
		viewHolder.checkBox.setOnClickListener(v -> {
			if (item.isFolder()) {
				if (!item.isChoice) {
					item.isChoice = true;
					choiceCount++;
					clickFolderList.add(item);
				}
				else {
					choiceCount--;
					item.isChoice = false;
					clickFolderList.remove(item);
				}
			}
			else {
				if (!item.isChoice) {
					item.isChoice = true;
					choiceCount++;
					clickFileList.add(item);
				}
				else {
					choiceCount--;
					item.isChoice = false;
					clickFileList.remove(item);
				}
			}
			choiceListener.choiceStateListener(choiceCount, clickFolderList, clickFileList);
			for (FileAndFolder fileAndFolder : dataList) {
				((ListBaseItem) fileAndFolder).isClick = true;
			}
			if (isEditStand) {
				this.notifyItemChanged(viewHolder.getAdapterPosition());
			}
			else {
				notifyDataSetChanged();
				isEditStand = true;
			}
		});
	}

	@Override
	protected void onCheckBoxClickEvent(ListBaseItem file, KnowledgeListBaseAdapter.ItemViewHolder viewHolder) {
		super.onCheckBoxClickEvent(file, viewHolder);
		final FileAndFolder item = (FileAndFolder) file;
		if (item.isFolder()) {
			if (item.isChoice)
				clickFolderList.add(item);
			else
				clickFolderList.remove(item);
		}
		else {
			if (item.isChoice)
				clickFileList.add(item);
			else
				clickFileList.remove(item);
		}
		if (choiceListener != null)
			choiceListener.choiceStateListener(choiceCount, clickFolderList, clickFileList);
	}

	/**
	 * 恢复原Item的状态
	 */
	public void restoreOriginalState(boolean isClick) {
		for (FileAndFolder fileAndFolder : dataList) {
			((ListBaseItem) fileAndFolder).isClick = false;
			if (!isClick) {
				((ListBaseItem) fileAndFolder).isChoice = false;
			}
		}
		choiceCount = 0;
		clickFolderList.clear();
		clickFileList.clear();
		isEditStand = false;
		notifyDataSetChanged();
	}

	@Override
	public void setCanChoice(boolean canCheck) {
		super.setCanChoice(canCheck);
		if (!canCheck)
			clickFolderList.clear();
		if (choiceListener != null)
			choiceListener.choiceStateListener(choiceCount, clickFolderList, clickFileList);
	}

	public interface OnChoiceListener {

		void choiceStateListener(int choiceCount, List<FileAndFolder> clickFolderList, List<FileAndFolder> clickFileList);

	}

	public interface onItemClickListener {

		void onClickListener(FileAndFolder fileAndFolder);

	}


}