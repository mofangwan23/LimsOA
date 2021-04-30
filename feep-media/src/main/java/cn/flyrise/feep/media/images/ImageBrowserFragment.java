package cn.flyrise.feep.media.images;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.images.adapter.ImagePreviewAdapter.OnImagePreviewClickListener;
import cn.flyrise.feep.media.images.bean.ImageItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * @author ZYP
 * @since 2017-10-20 14:08
 */
public class ImageBrowserFragment extends Fragment {

	private String mImagePath;
	private PhotoView mIvPreview;
	private OnImagePreviewClickListener mPreViewClickListener;

	public static ImageBrowserFragment newInstance(ImageItem imageItem, OnImagePreviewClickListener listener) {
		ImageBrowserFragment fragment = new ImageBrowserFragment();
		fragment.mImagePath = imageItem.path;
		fragment.mPreViewClickListener = listener;
		return fragment;
	}


	@Nullable @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.ms_fragment_image_browser, container, false);
		mIvPreview = contentView.findViewById(R.id.msImagePreview);
		RequestBuilder<Bitmap> requestBuilder = Glide.with(getActivity()).asBitmap();
		requestBuilder.load(mImagePath)
				.apply(new RequestOptions().placeholder(R.mipmap.ms_image_preview)
						.error(R.mipmap.ms_image_preview))
				.into(mIvPreview);

		mIvPreview.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if (mPreViewClickListener != null) {
					mPreViewClickListener.onImagePreviewClick();
				}
			}
		});
		return contentView;
	}
}
