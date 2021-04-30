package cn.flyrise.feep.media.images.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import cn.flyrise.feep.core.component.LargeTouchCheckBox;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.images.adapter.ImageSelectionAdapter.ImageSelectionViewHolder;
import cn.flyrise.feep.media.images.bean.ImageItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-17 17:38
 */
public class ImageSelectionAdapter extends RecyclerView.Adapter<ImageSelectionViewHolder> {

	private final int mItemSize;
	private final boolean isSingleChoice;

	private List<ImageItem> mImages;
	private final List<ImageItem> mSelectedImages;    // 这种相当于多创建了一堆冗余数据。
	private OnImageOperatedListener mOperatedListener;

	public ImageSelectionAdapter(int itemSize, boolean isSingleChoice, List<ImageItem> selectedImages) {
		this.mItemSize = itemSize;
		this.isSingleChoice = isSingleChoice;
		this.mSelectedImages = selectedImages;
	}

	public void setOnImageOperatedListener(OnImageOperatedListener listener) {
		this.mOperatedListener = listener;
	}

	public void setImages(List<ImageItem> images) {
		this.mImages = images;
		this.notifyDataSetChanged();
	}

	@Override public ImageSelectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ms_item_image_selection, parent, false);
		return new ImageSelectionViewHolder(convertView, mItemSize);
	}

	@Override public void onBindViewHolder(final ImageSelectionViewHolder holder, final int position) {
		final ImageItem imageItem = mImages.get(position);
		RequestBuilder<Bitmap> requestBuilder = Glide.with(holder.ivThumbnail.getContext()).asBitmap();
		requestBuilder.load(imageItem.path)
				.thumbnail(0.5F)
				.apply(new RequestOptions().placeholder(R.mipmap.ms_image_preview)
						.error(R.mipmap.ms_image_preview)
						.centerCrop()
						.override(mItemSize, mItemSize))
				.into(holder.ivThumbnail);

		holder.checkBox.setVisibility(isSingleChoice ? View.GONE : View.VISIBLE);
		holder.checkBox.setChecked(imageItem.isHasSelected());

		if (!isSingleChoice) {
			holder.checkBox.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					if (mOperatedListener != null) {
						changeCheckStates(holder, mOperatedListener.onImageCheckChange(imageItem, position) == 1);
						mOperatedListener.onImageCheckListenr();
					}
				}
			});
		}

		holder.ivThumbnail.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if (mOperatedListener != null) {
					mOperatedListener.onImageClick(imageItem, position);
				}
			}
		});
	}

	private void changeCheckStates(ImageSelectionViewHolder holder, boolean isChecked) {
		holder.checkBox.setChecked(isChecked);
		if (isChecked) {
			holder.ivThumbnail.setColorFilter(Color.parseColor("#50000000"));
		}
		else {
			holder.ivThumbnail.setColorFilter(Color.parseColor("#0F000000"));
		}
	}

	@Override public int getItemCount() {
		return mImages == null ? 0 : mImages.size();
	}

	public class ImageSelectionViewHolder extends RecyclerView.ViewHolder {

		private ImageView ivThumbnail;
		private LargeTouchCheckBox checkBox;


		public ImageSelectionViewHolder(View itemView, int itemSize) {
			super(itemView);
			ivThumbnail = (ImageView) itemView.findViewById(R.id.msIvThumbnail);
			checkBox = (LargeTouchCheckBox) itemView.findViewById(R.id.msCheckBox);

			ViewGroup.LayoutParams layoutParams = ivThumbnail.getLayoutParams();
			layoutParams.width = itemSize;
			layoutParams.height = itemSize;
			ivThumbnail.setLayoutParams(layoutParams);
		}
	}

	public interface OnImageOperatedListener {

		int onImageCheckChange(ImageItem imageItem, int position);

		void onImageClick(ImageItem imageItem, int position);

		void onImageCheckListenr();
	}
}
