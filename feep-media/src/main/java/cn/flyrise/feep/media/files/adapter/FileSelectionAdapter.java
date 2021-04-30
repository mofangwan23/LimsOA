package cn.flyrise.feep.media.files.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.files.FileItem;
import cn.flyrise.feep.media.files.adapter.FileSelectionAdapter.FileSelectionViewHolder;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-23 19:24
 */
public class FileSelectionAdapter extends RecyclerView.Adapter<FileSelectionViewHolder> {

	private final List<FileItem> mSelectedFiles;
	private final boolean isSingleChoice;
	private List<FileItem> mFiles;
	private OnFileItemClickListener mListener;

	public FileSelectionAdapter(List<FileItem> selectedFiles, boolean isSingleChoice) {
		this.mSelectedFiles = selectedFiles;
		this.isSingleChoice = isSingleChoice;
	}

	public void setFiles(List<FileItem> files) {
		this.mFiles = files;
		this.notifyDataSetChanged();
	}

	public void setOnFileItemClickListener(OnFileItemClickListener listener) {
		this.mListener = listener;
	}

	@Override public FileSelectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View selectionView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ms_item_file_selection, parent, false);
		return new FileSelectionViewHolder(selectionView);
	}

	@Override public void onBindViewHolder(final FileSelectionViewHolder holder, int position) {
		final FileItem fileItem = mFiles.get(position);
		holder.ivFileIcon.setImageResource(fileItem.thumbnailRes);
		holder.tvFileName.setText(fileItem.name);
		holder.fileCheckBox.setVisibility(fileItem.isDir() || !fileItem.name.contains(".") ? View.GONE : isSingleChoice ? View.GONE : View
				.VISIBLE);
		holder.fileCheckBox.setChecked(mSelectedFiles.contains(fileItem));
		holder.itemView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if (mListener != null) {
					int resultCode = mListener.onFileItemClick(fileItem);
					holder.fileCheckBox.setChecked(resultCode == 1);
				}
			}
		});
	}

	@Override public int getItemCount() {
		return mFiles == null ? 0 : mFiles.size();
	}

	public class FileSelectionViewHolder extends RecyclerView.ViewHolder {

		public ImageView ivFileIcon;
		public TextView tvFileName;
		public CheckBox fileCheckBox;

		public FileSelectionViewHolder(View itemView) {
			super(itemView);
			ivFileIcon = (ImageView) itemView.findViewById(R.id.msIvFileIcon);
			tvFileName = (TextView) itemView.findViewById(R.id.msTvFileName);
			fileCheckBox = (CheckBox) itemView.findViewById(R.id.msFileCheckBox);
		}
	}

	public interface OnFileItemClickListener {

		/**
		 * 文件操作 -2
		 * 添加成功 1
		 * 移除成功 -1
		 * 达到上限或者其他无法添加的情况 0
		 */
		int onFileItemClick(FileItem fileItem);
	}
}
