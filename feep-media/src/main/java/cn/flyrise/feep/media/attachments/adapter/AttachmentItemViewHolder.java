package cn.flyrise.feep.media.attachments.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.common.CircleProgressBar;

/**
 * @author ZYP
 * @since 2017-11-07 14:12
 */
public class AttachmentItemViewHolder extends RecyclerView.ViewHolder {

	public ImageView ivAttachmentIcon;
	public TextView tvAttachmentName;
	public TextView tvAttachmentSize;

	public CheckBox deleteCheckBox;
	public ImageView ivDownloadState;
	public ImageView ivDownloadSuccess;
	public CircleProgressBar downloadProgressBar;

	public AttachmentItemViewHolder(View itemView) {
		super(itemView);
		ivAttachmentIcon = (ImageView) itemView.findViewById(R.id.msIvFileIcon);
		tvAttachmentName = (TextView) itemView.findViewById(R.id.msTvFileName);
		tvAttachmentSize = (TextView) itemView.findViewById(R.id.msTvFileSize);

		deleteCheckBox = (CheckBox) itemView.findViewById(R.id.msCheckBox);
		ivDownloadState = (ImageView) itemView.findViewById(R.id.msIvDownloadState);
		ivDownloadSuccess = (ImageView) itemView.findViewById(R.id.msIvDownloadSuccess);
		downloadProgressBar = (CircleProgressBar) itemView.findViewById(R.id.msDownloadProgressBar);
	}
}