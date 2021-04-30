package cn.flyrise.feep.media.images.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.images.adapter.AlbumSelectionAdapter.ImageAlbumListViewHolder;
import cn.flyrise.feep.media.images.bean.Album;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-18 14:40
 */
public class AlbumSelectionAdapter extends RecyclerView.Adapter<ImageAlbumListViewHolder> {

	private String mDefaultAlbumId;
	private List<Album> mImageAlbums;
	private OnAlbumItemClickListener mItemClickListener;

	public AlbumSelectionAdapter(List<Album> imageAlbums) {
		this.mImageAlbums = imageAlbums;
	}

	public void setOnAlbumItemClickListener(OnAlbumItemClickListener itemClickListener) {
		this.mItemClickListener = itemClickListener;
	}

	public void setDefaultAlbumId(String albumId) {
		this.mDefaultAlbumId = albumId;
		this.notifyDataSetChanged();
	}

	@Override public ImageAlbumListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ms_item_album_selection, parent, false);
		return new ImageAlbumListViewHolder(convertView);
	}

	@Override public void onBindViewHolder(ImageAlbumListViewHolder holder, final int position) {
		final Album album = mImageAlbums.get(position);
		RequestBuilder<Bitmap> requestBuilder = Glide.with(holder.ivAlbum.getContext()).asBitmap();
		requestBuilder.load(album.cover)
				.apply(new RequestOptions().placeholder(R.mipmap.ms_image_preview)
						.error(R.mipmap.ms_image_preview)
						.centerCrop())
				.thumbnail(0.5F)
				.into(holder.ivAlbum);

		holder.itemView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if (mItemClickListener != null) {
					mItemClickListener.onAlbumItemClick(album, position);
				}
			}
		});
		holder.tvAlbumName.setText(album.name);
		holder.tvAlbumCount.setText(album.count + "张");
		holder.radioButton.setVisibility(TextUtils.equals(album.id, mDefaultAlbumId) ? View.VISIBLE : View.GONE);
	}

	@Override public int getItemCount() {
		return mImageAlbums == null ? 0 : mImageAlbums.size();
	}

	public class ImageAlbumListViewHolder extends RecyclerView.ViewHolder {

		private ImageView ivAlbum;
		private TextView tvAlbumName;
		private TextView tvAlbumCount;
		private RadioButton radioButton;

		public ImageAlbumListViewHolder(View itemView) {
			super(itemView);
			ivAlbum = (ImageView) itemView.findViewById(R.id.msIvAlbum);
			tvAlbumName = (TextView) itemView.findViewById(R.id.msTvAlbumName);
			tvAlbumCount = (TextView) itemView.findViewById(R.id.msTvAlbumCount);
			radioButton = (RadioButton) itemView.findViewById(R.id.msRadioButton);
		}
	}

	public interface OnAlbumItemClickListener {

		void onAlbumItemClick(Album album, int position);
	}

}
