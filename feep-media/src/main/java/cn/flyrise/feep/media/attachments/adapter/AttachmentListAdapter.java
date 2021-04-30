package cn.flyrise.feep.media.attachments.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.DownloadProgress;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.attachments.listener.IAttachmentItemHandleListener;
import cn.flyrise.feep.media.attachments.listener.IDownloadProgressCallback;
import cn.flyrise.feep.media.common.AttachmentUtils;
import cn.flyrise.feep.media.common.FileCategoryTable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-25 15:16
 */
public class AttachmentListAdapter extends RecyclerView.Adapter<AttachmentItemViewHolder> {

	private List<Attachment> mAttachments;
	private final List<Attachment> mToDeleteAttachments;            // 保存待删除的已选中附件

	private boolean isEditMode;
	private IAttachmentItemHandleListener mHandleListener;
	private IDownloadProgressCallback mProgressCallback;

	public AttachmentListAdapter() {
		mToDeleteAttachments = new ArrayList<>();
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

	public void setDownloadProgressCallback(IDownloadProgressCallback progressCallback) {
		this.mProgressCallback = progressCallback;
	}

	public void setOnAttachmentItemHandleListener(IAttachmentItemHandleListener listener) {
		this.mHandleListener = listener;
	}

	@Override public AttachmentItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ms_item_attachment, parent, false);
		return new AttachmentItemViewHolder(itemView);
	}

	@Override public void onBindViewHolder(AttachmentItemViewHolder holder, int position) {
		final Attachment attachment = mAttachments.get(position);
		if (attachment == null) return;
		holder.ivAttachmentIcon.setImageResource(FileCategoryTable.getIcon(attachment.type));
		holder.tvAttachmentName.setText(attachment.name);

		holder.deleteCheckBox.setChecked(mToDeleteAttachments.contains(attachment));
		holder.deleteCheckBox.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

		if (attachment instanceof NetworkAttachment) {
			// 检查附件是否已经下载成功
			File attachmentFile = AttachmentUtils.getDownloadedAttachment(attachment);
			if (attachmentFile == null) {
				// 未下载成功的附件，有些是获取不到 size 的（API 没有给过来）
				holder.tvAttachmentSize.setVisibility(attachment.size == 0 ? View.GONE : View.VISIBLE);
				holder.tvAttachmentSize.setText(Formatter.formatFileSize(CoreZygote.getContext(), attachment.size));
				holder.ivDownloadSuccess.setVisibility(View.GONE);
			}
			else {
				// 附件下载成功
				holder.tvAttachmentSize.setVisibility(View.VISIBLE);
				holder.ivDownloadSuccess.setVisibility(View.VISIBLE);
				holder.tvAttachmentSize.setText(Formatter.formatFileSize(CoreZygote.getContext(), attachmentFile.length()));
			}

			// 检查附件是否处于下载状态
			DownloadProgress progress = mProgressCallback == null ? null : mProgressCallback.downloadProgress(attachment);
			if (progress == null || progress.isCompleted()) {
				holder.ivAttachmentIcon.setColorFilter(Color.TRANSPARENT);
				holder.ivDownloadState.setVisibility(View.GONE);
				holder.downloadProgressBar.setVisibility(View.GONE);
			}
			else {
				holder.ivAttachmentIcon.setColorFilter(Color.parseColor("#88FFFFFF"));
				holder.ivDownloadState.setVisibility(View.VISIBLE);
				holder.ivDownloadState.setTag(progress.isRunning() ? 1 : 0);
				holder.ivDownloadState.setImageResource(progress.isRunning()
						? R.mipmap.ms_icon_download_state_pause
						: R.mipmap.ms_icon_download_state_restart);
				holder.ivDownloadSuccess.setVisibility(View.GONE);
				holder.downloadProgressBar.setVisibility(View.VISIBLE);
				holder.downloadProgressBar.setProgress(progress.getProgress());  // 设置进度
			}
		}
		else {
			// 普通本地附件
			holder.tvAttachmentSize.setText(Formatter.formatFileSize(CoreZygote.getContext(), attachment.size));
			holder.ivAttachmentIcon.setColorFilter(Color.TRANSPARENT);
			holder.ivDownloadState.setVisibility(View.GONE);
			holder.ivDownloadSuccess.setVisibility(View.GONE);
			holder.downloadProgressBar.setVisibility(View.GONE);
		}

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

		holder.ivDownloadState.setOnClickListener(v -> {            // 下载进度
			if (isEditMode) {
				return;
			}

			int state = (Integer) holder.ivDownloadState.getTag();
			if (state == 1) {                                       // running 状态，需要停止正在下载的任务
				mHandleListener.onAttachmentDownloadStopped(attachment);
				holder.ivDownloadState.setImageResource(R.mipmap.ms_icon_download_state_pause);
				holder.ivDownloadState.setTag(0);
				return;
			}

			mHandleListener.onAttachmentDownloadResume(attachment);
			holder.ivDownloadState.setImageResource(R.mipmap.ms_icon_download_state_restart);
			holder.ivDownloadState.setTag(1);
		});
	}

	@Override public int getItemCount() {
		return mAttachments == null ? 0 : mAttachments.size();
	}

	public int getToDeleteAttachmentSize() {
		return mToDeleteAttachments.size();
	}

	public void clearToDeleteAttachments() {
		mToDeleteAttachments.clear();
	}

	public List<Attachment> getToDeleteAttachments() {
		return mToDeleteAttachments;
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

	public List<Attachment> getAttachments() {
		return mAttachments;
	}
}