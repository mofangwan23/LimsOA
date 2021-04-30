package cn.flyrise.feep.media.attachments.adapter;

import android.graphics.Color;
import android.support.annotation.Keep;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.DownloadProgress;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.attachments.listener.IDownloadProgressCallback;
import cn.flyrise.feep.media.attachments.listener.INetworkAttachmentItemHandleListener;
import cn.flyrise.feep.media.common.AttachmentUtils;
import cn.flyrise.feep.media.common.FileCategoryTable;
import java.io.File;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-11-07 14:11
 */
public class NetworkAttachmentListAdapter extends RecyclerView.Adapter<AttachmentItemViewHolder> {

	private final List<NetworkAttachment> mAttachments;
	private IDownloadProgressCallback mProgressCallback;
	private INetworkAttachmentItemHandleListener mHandleListener;


	public NetworkAttachmentListAdapter(List<NetworkAttachment> attachments) {
		this.mAttachments = attachments;
	}

	public void setOnSimpleAttachmentItemHandleListener(INetworkAttachmentItemHandleListener listener) {
		this.mHandleListener = listener;
	}

	public void setDownloadProgressCallback(IDownloadProgressCallback progressCallback) {
		this.mProgressCallback = progressCallback;
	}

	@Override public AttachmentItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ms_item_attachment, parent, false);
		return new AttachmentItemViewHolder(itemView);
	}

	@Override public void onBindViewHolder(AttachmentItemViewHolder holder, int position) {
		final Attachment attachment = mAttachments.get(position);
		holder.ivAttachmentIcon.setImageResource(FileCategoryTable.getIcon(attachment.type));
		holder.tvAttachmentName.setText(attachment.name);
		holder.deleteCheckBox.setVisibility(View.GONE);

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
			FELog.i(attachment.name + " : " + progress.getProgress());
			holder.downloadProgressBar.setProgress(progress.getProgress());  // 设置进度
		}

		holder.itemView.setOnClickListener(v -> {                   // 单击
			if (mHandleListener != null) {
				mHandleListener.onAttachmentItemClick(position, attachment);
			}
		});
		holder.ivDownloadState.setOnClickListener(v -> {            // 下载进度
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
		return CommonUtil.isEmptyList(mAttachments) ? 0 : mAttachments.size();
	}
}
