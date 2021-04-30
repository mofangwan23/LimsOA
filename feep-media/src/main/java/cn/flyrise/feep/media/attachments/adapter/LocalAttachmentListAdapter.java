package cn.flyrise.feep.media.attachments.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.listener.ILocalAttachmentItemHandleListener;
import cn.flyrise.feep.media.common.FileCategoryTable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-11-08 15:57
 */
public class LocalAttachmentListAdapter extends RecyclerView.Adapter<AttachmentItemViewHolder> {

	private boolean isEditMode;
	private List<Attachment> mAttachments;
	private final List<Attachment> mToDeleteAttachments;            // 保存待删除的已选中附件
	private ILocalAttachmentItemHandleListener mHandleListener;

	public LocalAttachmentListAdapter() {
		this.mToDeleteAttachments = new ArrayList<>();
	}

	public void setAttachments(List<Attachment> attachments) {
		this.mAttachments = attachments;
		this.notifyDataSetChanged();
	}

	public void setEditMode(boolean editMode) {
		this.isEditMode = editMode;
	}

	public boolean isEditMode() {
		return isEditMode;
	}

	public void setOnLocalAttachmentItemHandleListener(ILocalAttachmentItemHandleListener listener) {
		this.mHandleListener = listener;
	}

	@Override public AttachmentItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ms_item_attachment, parent, false);
		return new AttachmentItemViewHolder(itemView);
	}

	@Override public void onBindViewHolder(AttachmentItemViewHolder holder, int position) {
		final Attachment attachment = mAttachments.get(position);
		FELog.i("position = " + position);
		holder.ivAttachmentIcon.setImageResource(FileCategoryTable.getIcon(attachment.type));
		holder.tvAttachmentName.setText(attachment.name);
		holder.deleteCheckBox.setChecked(mToDeleteAttachments.contains(attachment));
		holder.deleteCheckBox.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

		holder.tvAttachmentSize.setText(Formatter.formatFileSize(CoreZygote.getContext(), attachment.size));
		holder.ivAttachmentIcon.setColorFilter(Color.TRANSPARENT);

		holder.ivDownloadState.setVisibility(View.GONE);
		holder.ivDownloadSuccess.setVisibility(View.GONE);
		holder.downloadProgressBar.setVisibility(View.GONE);

		holder.itemView.setOnClickListener(v -> {                   // 单击
			if (mHandleListener != null) {
				mHandleListener.onAttachmentItemClick(position, attachment);
			}
		});

		holder.itemView.setOnLongClickListener(v -> {               // 长按进入编辑状态
			if (mHandleListener != null) {
				mToDeleteAttachments.add(attachment);
				mHandleListener.onAttachmentItemLongClick(attachment);
			}
			return true;
		});

		holder.deleteCheckBox.setOnClickListener(v -> {             // 删除事件
			if (mToDeleteAttachments.contains(attachment)) {
				holder.deleteCheckBox.setChecked(false);
				mToDeleteAttachments.remove(attachment);
			}
			else {
				holder.deleteCheckBox.setChecked(true);
				mToDeleteAttachments.add(attachment);
			}

			if (mHandleListener != null) {
				mHandleListener.onAttachmentItemToBeDeleteCheckChange();
			}
		});
	}

	@Override public int getItemCount() {
		return CommonUtil.isEmptyList(mAttachments) ? 0 : mAttachments.size();
	}

	public void clearToDeleteAttachments() {
		mToDeleteAttachments.clear();
	}

	public List<Attachment> getToDeleteAttachments() {
		return mToDeleteAttachments;
	}

	public int getToDeleteAttachmentSize() {
		return mToDeleteAttachments.size();
	}

	public void addAttachmentToDelete(int position, Attachment attachment) {
		if (mToDeleteAttachments.contains(attachment)) {
			mToDeleteAttachments.remove(attachment);
		}
		else {
			mToDeleteAttachments.add(attachment);
		}
		notifyItemChanged(position);
	}

	public void notifyAllAttachmentDeleteState(boolean isDeleteAll) {
		if (!isDeleteAll) {
			mToDeleteAttachments.clear();
			notifyDataSetChanged();
			return;
		}

		for (Attachment attachment : mAttachments) {
			if (!mToDeleteAttachments.contains(attachment)) {
				mToDeleteAttachments.add(attachment);
			}
		}
		notifyDataSetChanged();
	}
}
