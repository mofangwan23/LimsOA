package cn.flyrise.feep.media.files.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.files.FileIndicator;
import cn.flyrise.feep.media.files.adapter.FileIndicatorAdapter.FileIndicatorViewHolder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-23 19:24
 */
public class FileIndicatorAdapter extends RecyclerView.Adapter<FileIndicatorViewHolder> {

	// pair.first = file path and pair.second = file nae
	private final List<FileIndicator> mFileIndicators = new ArrayList<>();
	private OnFileIndicatorClickListener mListener;

	@Override public FileIndicatorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View indicatorView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ms_item_file_indicator, parent, false);
		return new FileIndicatorViewHolder(indicatorView);
	}

	@Override public void onBindViewHolder(FileIndicatorViewHolder holder, int position) {
		final FileIndicator indicator = mFileIndicators.get(position);
		holder.tvIndicator.setText(indicator.name);
		holder.itemView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if (mListener != null) {
					mListener.onFileIndicatorClick(indicator);
				}
			}
		});
	}

	@Override public int getItemCount() {
		return mFileIndicators == null ? 0 : mFileIndicators.size();
	}

	public void setOnFileIndicatorClickListener(OnFileIndicatorClickListener listener) {
		this.mListener = listener;
	}

	/**
	 * 移除指定 indicator 之后的元素
	 */
	public void removeIndicatorAfter(FileIndicator target) {
		Iterator<FileIndicator> iterator = mFileIndicators.iterator();
		while (iterator.hasNext()) {
			FileIndicator next = iterator.next();
			String nextPath = next.path;
			String targetPath = target.path;
			if (!TextUtils.equals(nextPath, targetPath)
					&& nextPath.startsWith(targetPath)) {
				iterator.remove();
			}
		}
		notifyDataSetChanged();
	}

	public void addIndicator(FileIndicator indicator) {
		mFileIndicators.add(indicator);
		this.notifyDataSetChanged();
	}

	/**
	 * 删除最后一个元素，并返回新的末尾元素
	 */
	public FileIndicator forwardIndicator() {
		mFileIndicators.remove(mFileIndicators.size() - 1);
		notifyDataSetChanged();
		return mFileIndicators.get(mFileIndicators.size() - 1);
	}

	public class FileIndicatorViewHolder extends RecyclerView.ViewHolder {

		private TextView tvIndicator;

		public FileIndicatorViewHolder(View itemView) {
			super(itemView);
			tvIndicator = (TextView) itemView.findViewById(R.id.msTvFileIndicator);
		}
	}

	public interface OnFileIndicatorClickListener {

		void onFileIndicatorClick(FileIndicator indicator);
	}

}
